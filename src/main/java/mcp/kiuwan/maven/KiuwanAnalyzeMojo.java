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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;


@Mojo(name = "analyze")
public class KiuwanAnalyzeMojo extends AbstractMojo {
	
	private static final String LOGPREFIX = "[kiuwan] ";
	private static final String LOGFILENAME = "analyze";

	private static final String ANALYSIS_SCOPE_BASELINE = "baseline";
	private static final String ANALYSIS_SCOPE_COMPLETE_DELIVERY = "completeDelivery";
	
	@Parameter(property = "kiuwan.home", defaultValue = "${env.KIUWAN_LOCAL_ANALYZER_HOME}")
	private String kHome;
	
	@Parameter(property = "kiuwan.sourcePath", defaultValue = "${basedir}")
	private String kSourcePath;
	
	@Parameter(property = "kiuwan.softwareName", defaultValue = "${project.name}")
	private String kSoftwareName;
	
	@Parameter(property = "kiuwan.label", defaultValue = "${project.version}")
	private String kLabel;
	
	@Parameter(property = "kiuwan.wait-for-results", defaultValue = "false")
	private boolean kWaitForResults;
		
	@Parameter(property = "kiuwan.analysis-scope", defaultValue = "baseline")
	private String kAnalysisScope;
		
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
	

	public String getKHome() {
		return kHome;
	}
	public void setKHome(String kHome) {
		this.kHome = kHome;
	}
	
	public String getKSourcePath() {
		return kSourcePath;
	}
	public void setKSourcePath(String kSourcePath) {
		this.kSourcePath = kSourcePath;
	}

	public String getKSoftwareName() {
		return kSoftwareName;
	}
	public void setKSoftwareName(String kSoftwareName) {
		this.kSoftwareName = kSoftwareName;
	}
	
	public String getKLabel() {
		return kLabel;
	}
	public void setKLabel(String kLabel) {
		this.kLabel = kLabel;
	}
	
	public String getKAnalysisScope() {
		return kAnalysisScope;
	}
	public void setKAnalysisScope(String kAnalysisScope) {
		this.kAnalysisScope = kAnalysisScope;
	}
	
	public boolean getKWaitForResults() {
		return kWaitForResults;
	}
	public void setKWaitForResults(boolean kWaitForResults) {
		this.kWaitForResults = kWaitForResults;
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
			execExecutable = StringUtils.trim(getKHome()) + "\\bin\\agent.cmd";			
		} else {
			execExecutable = StringUtils.trim(getKHome()) + "/bin/agent.sh";
		}
		
		// builds args
		execArgs = new ArrayList<>();
		execArgs.add("--softwareName");
		execArgs.add(StringUtils.trim(getKSoftwareName()));
		execArgs.add("--sourcePath");
		execArgs.add(StringUtils.trim(getKSourcePath()));
		
		if (StringUtils.isNotEmpty(getKLabel())) {
			execArgs.add("--label");
			execArgs.add(StringUtils.trim(getKLabel()));
		}
		
		if (ANALYSIS_SCOPE_BASELINE.equalsIgnoreCase(StringUtils.trim(getKAnalysisScope()))) {
			execArgs.add("--analysis-scope");
			execArgs.add(ANALYSIS_SCOPE_BASELINE);			
		} else {
			execArgs.add("--analysis-scope");
			execArgs.add(ANALYSIS_SCOPE_COMPLETE_DELIVERY);		
			
			execArgs.add("--change-request-status");
			execArgs.add("inprogress");		
		}
		
		if (getKWaitForResults()) {
			execArgs.add("--wait-for-results");
		}

		// builds output file.
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
		LocalDateTime now = LocalDateTime.now();
		String timestamp = dtf.format(now);
		File outputFile = new File(buildDirectory + "/kiuwan/" + LOGFILENAME + "-" + timestamp + ".log");
		outputFile.getParentFile().mkdirs();		
		outputFileName = outputFile.getAbsolutePath();
		
		return execExecutable + " " + execArgs;
	}
	
}

