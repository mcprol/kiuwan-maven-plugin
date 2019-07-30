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

import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.util.List;

/**
 * Utility class to build objects needed for Maven Exec Plugin
 * 	https://github.com/mojohaus/exec-maven-plugin
 * 	https://github.com/TimMoore/mojo-executor
 */
class ExecPluginHelper {

	public static Plugin createMavenExecPlugin() {
		Plugin execPlugin = new Plugin();
		execPlugin.setArtifactId("exec-maven-plugin");
		execPlugin.setGroupId("org.codehaus.mojo");
		execPlugin.setVersion("1.6.0");
		
		return execPlugin;
	}
	
	
	public static Xpp3Dom createExecPluginConfiguration(String executable, List<String> args, String outputFileName) {
		Xpp3Dom configuration = MojoExecutor.configuration();
		
		// executable.
		Xpp3Dom executableConfiguration = new Xpp3Dom("executable");
		executableConfiguration.setValue(executable);
		configuration.addChild(executableConfiguration);
		
		// args.
		Xpp3Dom argsConfiguration = new Xpp3Dom("arguments");
		for (String arg: args) {
			Xpp3Dom argx = new Xpp3Dom("argument");
			argx.setValue(arg);
			argsConfiguration.addChild(argx);
		};
		configuration.addChild(argsConfiguration);
		
		// outputFile.
		Xpp3Dom outputFileConfiguration = new Xpp3Dom("outputFile");
		outputFileConfiguration.setValue(outputFileName);
		configuration.addChild(outputFileConfiguration);
				
		return configuration;
	}	
	
}