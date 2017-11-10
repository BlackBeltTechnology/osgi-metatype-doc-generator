package hu.blackbelt.osgi.plugin.metatype;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.osgi.xmlns.metatype.v1_2.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Mojo(name = "genDoc")
public class DocumentGeneratorMojo extends AbstractMojo {

    private static final String FREEMARKER_TEMPLATE_DIR = "/templates";
    private static final String OCD_TEMPLATE_NAME = "ocd.ftl";
    private static final String COMPONENTS_FILENAME = "components.txt";
    private static final String OUTPUT_FILENAME_PATTERN = "ocd-{0}.html";

    @Parameter(property = "location", defaultValue = "${project.build.directory}/classes/OSGI-INF/metatype")
    String location;

    @Parameter(property = "templates")
    String templates;

    @Parameter(defaultValue = "${project.groupId}:${project.artifactId}", readonly = true)
    String project;

    @Parameter(defaultValue = "${project.build.directory}/classes/META-INF/osgi-metatype")
    String output;

    private static JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(MetaData.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException("Unable to initialize JAXB");
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        final File file = new File(location);
        getLog().info("Processing OSGI metadata in " + file.getAbsolutePath());

        final Path targetDir = getTargetDir();
        final Map<String, Tocd> ocds = new TreeMap<>();
        final Map<String, Tdesignate> designates = new TreeMap<>();

        try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(location))) {
            for (final Path path : directoryStream) {
                final MetaData metaData = readMetaData(path);
                for (final Tocd ocd : metaData.getOCDS()) {
                    ocds.put(ocd.getId(), ocd);
                }

                for (final Tdesignate designate : metaData.getDesignates()) {
                    designates.put(designate.getPid(), designate);
                }
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Unable to process OSGi metadata", ex);
        }

        try {
            final Writer listWriter = new FileWriter(targetDir.resolve(COMPONENTS_FILENAME).toFile());
            for (final Map.Entry<String, Tdesignate> designate : designates.entrySet()) {
                listWriter.append(designate.getKey()).append(":").
                        append(MessageFormat.format(OUTPUT_FILENAME_PATTERN, designate.getValue().getObject().getOcdref()));
            }
            listWriter.flush();
            listWriter.close();

            for (final Map.Entry<String, Tocd> me : ocds.entrySet()) {
                final Map<String, Object> model = new HashMap<>();
                model.put("project", project);
                model.put("ocd", me.getValue());
                renderHtml(model, me.getKey(), targetDir);
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Unable to initialize templates for OSGi metadata", ex);
        }
    }

    private void renderHtml(final Map<String, Object> model, String ocdId, Path targetDir) throws MojoFailureException, IOException {
        final Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        if (templates != null) {
            configuration.setDirectoryForTemplateLoading(new File(templates));
        } else {
            configuration.setTemplateLoader(new ClassTemplateLoader(DocumentGeneratorMojo.class, FREEMARKER_TEMPLATE_DIR));
        }
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);


        final Template template = configuration.getTemplate(OCD_TEMPLATE_NAME);
        try {
            final String targetFileName = MessageFormat.format(OUTPUT_FILENAME_PATTERN, ocdId);
            template.process(model, new FileWriter(targetDir.resolve(targetFileName).toFile()));
        } catch (TemplateException ex2) {
            throw new MojoFailureException("Invalid OCD template", ex2);
        }
    }

    private MetaData readMetaData(final Path path) throws MojoExecutionException {
        getLog().debug("Processing file: " + path + " ...");

        try {
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (MetaData) unmarshaller.unmarshal(path.toFile());
        } catch (JAXBException ex) {
            throw new MojoExecutionException("Unable to parse OSGI metadata", ex);
        }
    }

    private Path getTargetDir() throws MojoExecutionException {
        try {
            return Files.createDirectories(Paths.get(output));
        } catch (IOException ex) {
            throw new MojoExecutionException("Unable to create target directory", ex);
        }
    }
}
