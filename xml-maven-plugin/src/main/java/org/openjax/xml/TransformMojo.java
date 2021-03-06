/* Copyright (c) 2008 OpenJAX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.openjax.xml;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashSet;

import javax.xml.transform.TransformerException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.libj.net.URIs;
import org.libj.util.Dates;
import org.openjax.maven.mojo.MojoUtil;
import org.openjax.xml.transform.Transformer;

@Mojo(name="transform", defaultPhase=LifecyclePhase.COMPILE)
@Execute(goal="transform")
public class TransformMojo extends XmlMojo {
  @Parameter(property="destDir", required=true)
  private String destDir;

  @Parameter(property="rename")
  private String rename;

  @Parameter(property="stylesheet")
  private File stylesheet;

  @Override
  public void execute(final LinkedHashSet<URI> uris) throws MojoExecutionException, MojoFailureException {
    try {
      for (final URI uri : uris) {
        final String outFileName = MojoUtil.getRenamedFileName(uri.toString(), rename);
        final File destFile = new File(destDir, outFileName);
        final String inFilePath = URIs.isLocalFile(uri) ? CWD.relativize(new File(uri).getAbsoluteFile().toPath()).toString() : uri.toString();

        final long lastModified;
        if (destFile.exists() && (lastModified = uri.toURL().openConnection().getLastModified()) <= destFile.lastModified() && destFile.lastModified() < lastModified + Dates.MILLISECONDS_IN_DAY) {
          getLog().info("Pre-transformed: " + inFilePath);
        }
        else {
          final String outFilePath = CWD.relativize(destFile.getAbsoluteFile().toPath()).toString();
          getLog().info("   Transforming: " + inFilePath + " -> " + outFilePath);

          Transformer.transform(stylesheet.toURI(), uri, destFile);
        }
      }
    }
    catch (final IOException | TransformerException e) {
      throw new MojoExecutionException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
    }
  }
}