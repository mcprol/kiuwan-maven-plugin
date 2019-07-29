# kiuwan-maven-plugin
A simple maven plugin to run Kiuwan Local Analyzer from your POM files.

This plugin is really a shortcut (launcher, runner, etc) for the Kiuwan Local Analyzer command line script.

I'm assuming that you have an KiuwanLocalAnalyzer installed and configured in your machine. Also that you are familiar with its use, and the different options for 'baseline' and 'deliveries'.

## install.

Here are the steps to install this maven plugin in your maven installation.
* 1. Clone or download this repo

* 2. At root directory, run the following maven comand to compile:

	> mvn clean install
	
* 3. Deploy the plugin in your maven local repository:

	> mvn deploy -DaltDeploymentRepository=local-repository::default::file://d:/apache-maven-local-repository

* 4. define a new OS environment variable, with the location of your Kiuwan Local Analizer:

	(windows) set KIUWAN_LOCAL_ANALYZER_HOME=C:\KiuwanLocalAnalyzer
	(unix) export KIUWAN_LOCAL_ANALYZER_HOME=/opt/KiuwanLocalAnalyzer

## run.
To use this plugin in your projects:
* 1. edit your pom.xml and add the plugin in the 'build' section:

	<build>
		<plugins>
			<plugin>
				<groupId>mcp.kiuwan.maven</groupId>
				<artifactId>kiuwan-maven-plugin</artifactId>
				<version>0.1</version>
			</plugin>
		</plugins>
	</build>

* 2. from your project root directory, execute:

	> mvn kiuwan:analyze
	
or, you can overwrite some options:	

	> mvn kiuwan:analyze -Dkiuwan.softwareName=spiracle 

output (out and err) from above commands is left at file:

	target/kiuwan/analyze-{timestamp}.log
	
* 3. Also, you can insert the 'analyze' goal in a maven phase, like test:

	<plugin>
		<groupId>mcp.kiuwan.maven</groupId>
		<artifactId>kiuwan-maven-plugin</artifactId>
		<version>0.1</version>
		
		<executions>  
			<execution>  
				<phase>test</phase>  
				<goals>  
					<goal>analyze</goal>  
				</goals>
			</execution>
		</executions>
	</plugin>

and run maven with the standard options only:

	> mvn clean install	
	
## options.
These are the valid options that you can overwrite in your command line:

	@Parameter(property = "kiuwan.home", defaultValue = "${env.KIUWAN_LOCAL_ANALYZER_HOME}")
	@Parameter(property = "kiuwan.sourcePath", defaultValue = "${basedir}")
	@Parameter(property = "kiuwan.softwareName", defaultValue = "${project.name}")
	@Parameter(property = "kiuwan.label", defaultValue = "${project.version}")
	@Parameter(property = "kiuwan.wait-for-results", defaultValue = "false")
	@Parameter(property = "kiuwan.analysis-scope", defaultValue = "baseline")

## examples of running 'baseline' analysis

	> mvn kiuwan:analyze
	> mvn kiuwan:analyze -Dkiuwan.label="version-2.3.0" 

## examples of running 'delivery' analysis

	> mvn kiuwan:analyze -Dkiuwan.analysis-scope=completeDelivery
	> mvn kiuwan:analyze -Dkiuwan.analysis-scope=completeDelivery -Dkiuwan.wait-for-results=true

if 'kiuwan audit' fails, KLA returns an error code 10, and maven build fails. An error message is dumped to consoe:

	[ERROR] [kiuwan] Command execution failed.
	[ERROR] [kiuwan] Check error details at: D:\_github_mcprol\kiuwan-maven-plugin-test-spiracle-master\target\kiuwan\analyze-20190729172718625.log
	[ERROR] [kiuwan] Audit overall result = FAIL
	[ERROR] [kiuwan] Analysis created in Kiuwan with code: A-7e3-16c3e1cac0a
	[ERROR] [kiuwan] Analysis results URL: https://www.kiuwan.com/saas/web/dashboard/delivery?h=C65HU8UUN5I5KW5CA6N5LAW5EP8TNFN4NELR2WYMSAOZEC74WFFO0DY1Y259HI32
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD FAILURE
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 40.254 s
	[INFO] Finished at: 2019-07-29T17:27:58+02:00
	[INFO] Final Memory: 9M/245M
	[INFO] ------------------------------------------------------------------------
	[ERROR] Failed to execute goal mcp.kiuwan.maven:kiuwan-maven-plugin:0.1:analyze (default-cli) on project spiracle: Command execution failed.: Process exited with an error: 10 (Exit value: 10) -> [Help 1]

## references.
Following links help me to understand how a maven plugin works:

[https://dzone.com/articles/a-simple-maven-3-plugin](https://dzone.com/articles/a-simple-maven-3-plugin)

[http://www.vineetmanohar.com/2009/11/3-ways-to-run-java-main-from-maven/](http://www.vineetmanohar.com/2009/11/3-ways-to-run-java-main-from-maven/)

[https://www.adictosaltrabajo.com/2007/05/04/plugins-maven/](https://www.adictosaltrabajo.com/2007/05/04/plugins-maven/)

[https://github.com/mojohaus/exec-maven-plugin](https://github.com/mojohaus/exec-maven-plugin)

[https://github.com/TimMoore/mojo-executor](https://github.com/TimMoore/mojo-executor)

[https://github.com/cko/predefined_maven_properties/blob/master/README.md](https://github.com/cko/predefined_maven_properties/blob/master/README.md)

[https://books.sonatype.com/mvnref-book/reference/writing-plugins-sect-custom-plugin.html](https://books.sonatype.com/mvnref-book/reference/writing-plugins-sect-custom-plugin.html)

[https://github.com/vaeroon/mavenus](https://github.com/vaeroon/mavenus)
