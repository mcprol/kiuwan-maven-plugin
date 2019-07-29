// MIT License
//
// Copyright (c) 2019 Marcos Cacabelos Prol
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.


package mcp.kiuwan.maven;


import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;


@Mojo(name = "analyze")
public class KiuwanAnalyzeMojo extends AbstractMojo {
	
	private static final String LOGPREFIX = "[kiuwan] ";
	private static final String LOGFILENAME = "analyze";

	private static final String ANALYSIS_SCOPE_BASELINE = "baseline";
	private static final String ANALYSIS_SCOPE_COMPLETE_DELIVERY = "completeDelivery";
	
	@Parameter(property = "kiuwan.home", defaultValue = "${env.KIUWAN_LOCAL_ANALYZER_HOME}")
	private String home;
	
	@Parameter(property = "kiuwan.sourcePath", defaultValue = "${basedir}")
	private String sourcePath;
	
	@Parameter(property = "kiuwan.softwareName", defaultValue = "${project.name}")
	private String softwareName;
	
	@Parameter(property = "kiuwan.label", defaultValue = "${project.version}")
	private String label;
	
	@Parameter(property = "kiuwan.wait-for-results", defaultValue = "false")
	private boolean waitForResults;
		
	@Parameter(property = "kiuwan.analysis-scope", defaultValue = "baseline")
	private String analysisScope;
		
	@Parameter(property = "kiuwan.additionalOptions", defaultValue = "")
	private String additionalOptions;
	
	@Parameter(property = "kiuwan.extraParams", defaultValue = "")
	private String extraParams;
			
	@Parameter(property = "kiuwan.timestampInLogFilename", defaultValue = "true")
	private boolean timestampInLogFilename;
		
	@Parameter(property = "kiuwan.create", defaultValue = "false")
	private boolean create;
		
	@Parameter(readonly = true, required = true, defaultValue = "${project.build.directory}")
	private File buildDirectory;

	@Component
	private MavenProject mavenProject;

	@Component
	private MavenSession mavenSession;

	@Component
	private BuildPluginManager pluginManager;
	
	
	private String execExecutable;
	private List<String> execArgs;
	private String outputFileName;
	

	public String getHome() {
		return home;
	}
	public void setHome(String home) {
		this.home = home;
	}
	
	public String getSourcePath() {
		return sourcePath;
	}
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getSoftwareName() {
		return softwareName;
	}
	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean getTimestampInLogFilename() {
		return timestampInLogFilename;
	}
	public void setTimestampInLogFilename(boolean timestampInLogFilename) {
		this.timestampInLogFilename = timestampInLogFilename;
	}	
	
	public String getAnalysisScope() {
		return analysisScope;
	}
	public void setAnalysisScope(String analysisScope) {
		this.analysisScope = analysisScope;
	}
	
	public String getAdditionalOptions() {
		return additionalOptions;
	}
	public void setAdditionalOptions(String additionalOptions) {
		this.additionalOptions = additionalOptions;
	}
	
	public String getExtraParams() {
		return extraParams;
	}
	public void setExtraParams(String extraParams) {
		this.extraParams = extraParams;
	}
	
	public boolean getWaitForResults() {
		return waitForResults;
	}
	public void setWaitForResults(boolean waitForResults) {
		this.waitForResults = waitForResults;
	}	
	
	public boolean getCreate() {
		return create;
	}
	public void setCreate(boolean create) {
		this.create = create;
	}	
	
	
	public void execute() throws MojoExecutionException {
		getLog().info(LOGPREFIX + "Running " + buildCommandLine());
		runExecMojo();
	}

	
	private void runExecMojo() throws MojoExecutionException{
		Plugin execPlugin = ExecPluginHelper.createMavenExecPlugin();
		Xpp3Dom configuration = ExecPluginHelper.createExecPluginConfiguration(execExecutable, execArgs, outputFileName);
		
		try {
			executeMojo(execPlugin, "exec", configuration, executionEnvironment(mavenProject, mavenSession, pluginManager));
		} catch (MojoExecutionException e) {
			getLog().error(LOGPREFIX + e.getMessage());
			getLog().error(LOGPREFIX + "Check error details at: " + outputFileName);
			Utils.logException(getLog(), e, outputFileName);
			throw e;
		}		
	}	
	
	
	private String buildCommandLine() {		
		// builds executable name.	
		if (Os.isValidFamily(Os.FAMILY_WINDOWS)) {
			execExecutable = StringUtils.trim(getHome()) + "\\bin\\agent.cmd";			
		} else {
			execExecutable = StringUtils.trim(getHome()) + "/bin/agent.sh";
		}
		
		// builds args
		execArgs = new ArrayList<>();
		execArgs.add("--softwareName");
		execArgs.add(StringUtils.trim(getSoftwareName()));
				
		if (getCreate() && ANALYSIS_SCOPE_BASELINE.equalsIgnoreCase(StringUtils.trim(getAnalysisScope()))) {
			execArgs.add("--create");
		}
			
		execArgs.add("--sourcePath");
		execArgs.add(StringUtils.trim(getSourcePath()));
		
		if (StringUtils.isNotEmpty(getLabel())) {
			execArgs.add("--label");
			execArgs.add(StringUtils.trim(getLabel()));
		}
		
		if (ANALYSIS_SCOPE_BASELINE.equalsIgnoreCase(StringUtils.trim(getAnalysisScope()))) {
			execArgs.add("--analysis-scope");
			execArgs.add(ANALYSIS_SCOPE_BASELINE);			
		} else {
			execArgs.add("--analysis-scope");
			execArgs.add(ANALYSIS_SCOPE_COMPLETE_DELIVERY);		
			
			execArgs.add("--change-request-status");
			execArgs.add("inprogress");		
		}
		
		if (StringUtils.isNotEmpty(getAdditionalOptions())) {
			String[] options = StringUtils.trim(getAdditionalOptions()).split("\\s+");
			for (String option: options) {
				execArgs.add(StringUtils.trim(option));
			}
		}
		
		if (getWaitForResults()) {
			execArgs.add("--wait-for-results");
		}

		if (StringUtils.isNotEmpty(getExtraParams())) {
			String[] params = StringUtils.trim(getExtraParams()).split("\\s+");
			for (String param: params) {
				execArgs.add(StringUtils.trim(param));
			}
		}
		
		// builds output file.
		String timestamp = "";
		if (getTimestampInLogFilename()) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
			LocalDateTime now = LocalDateTime.now();
			timestamp = "-" + dtf.format(now);
		}
		File outputFile = new File(buildDirectory + "/kiuwan/" + LOGFILENAME + timestamp + ".log");
		outputFile.getParentFile().mkdirs();		
		outputFileName = outputFile.getAbsolutePath();
		
		return execExecutable + " " + execArgs;
	}
	
}

