/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is the maven-sar-plugin.
 *
 * The Initial Developer of the Original Code is
 * the Vermont Department of Taxes.
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Tom Cort <tom.cort@state.vt.us>
 *
 * ***** END LICENSE BLOCK ***** */

package net.sf.mavensar;

import java.io.File;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import org.codehaus.plexus.archiver.jar.JarArchiver;

/**
 * Creates a Service Archive.
 * 
 * @goal sar
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class SarMojo extends AbstractMojo {

    /**
     * The Project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The Project Helper.
     * 
     * @component
     */
    private MavenProjectHelper helper;

    /**
     * Archive Configuration.
     * 
     * @parameter
     */
    private MavenArchiveConfiguration archiveConfig = new MavenArchiveConfiguration();

    /**
     * The JAR Archiver.
     * 
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#jar}"
     * @required
     */
    private JarArchiver jarArchiver;

    /**
     * Directory where the sar file should be written to.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDir;

    /**
     * Directory containing the classes (usually target/classes).
     * 
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classesDir;

    /**
     * Name of the generated SAR (without the ".sar" extension).
     * 
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String targetFile;

    /**
     * Classifier to add to the artifact generated.
     * 
     * @parameter
     */
    private String classifier;

    /**
     * List of files to exclude from the SAR.
     */
    private static final String[] EXCLUDES = new String[] { "**/*.html" };

    /**
     * List of files in include in the SAR.
     */
    private static final String[] INCLUDES = new String[] { "**/*.xml", "**/*.class" };

    /**
     * Executes the task. In this case, packages the sar.
     * 
     * @throws MojoExecutionException
     *         on archiver error.
     */
    public final void execute() throws MojoExecutionException {

        getLog().info("====== MAVEN-SAR-PLUGIN ======");
        getLog().info("targetDir  [" + targetDir + "]");
        getLog().info("targetFile [" + targetFile + ".sar]");
        getLog().info("classesDir [" + classesDir.toString() + "]");

        File sar = new File(targetDir, targetFile + ".sar");

        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(jarArchiver);
        archiver.setOutputFile(sar);

        try {

            // Attempt to locale jboss-service.xml, fail if not found.
            File jbossServiceXml = new File(classesDir.toString() + "/META-INF/jboss-service.xml");

            if (!jbossServiceXml.exists()) {
                String msg = classesDir.toString() + "/META-INF/jboss-service.xml was not found.";
                throw new MojoExecutionException(msg);
            }

            archiver.getArchiver().addDirectory(classesDir, INCLUDES, EXCLUDES);
            archiver.createArchive(project, archiveConfig);

            if (classifier == null) {
                project.getArtifact().setFile(sar);
            } else {
                helper.attachArtifact(project, "sar", classifier, sar);
            }

        } catch (Exception e) {
            throw new MojoExecutionException("Error packaging SAR", e);
        }

        getLog().info("====== MAVEN-SAR-PLUGIN ======");
    }
}
