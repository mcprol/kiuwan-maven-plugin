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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

class Utils {
	
	private static final String LOGPREFIX = "[kiuwan] ";
	
	public static void logException(Log logger, MojoExecutionException e, String outputFileName) throws MojoExecutionException {
		Throwable cause = e.getCause();
		if (null != cause) {
			try {
				String message = cause.getMessage();
				if (StringUtils.isNotEmpty(message)) {
					// Process exited with an error: 10 (Exit value: 10)
					Scanner scanner = new Scanner(message);
					while (scanner.hasNext()) {
						if (scanner.hasNextInt()) {
							int errorCode = scanner.nextInt();
							if (errorCode == 10) {
								logger.error(LOGPREFIX + "Audit overall result = FAIL");
								
								List<String> lines = Files.readAllLines(Paths.get(outputFileName));
								for (String line: lines) {
									if (line.contains("Analysis created in Kiuwan with code") || line.contains("Analysis results URL")) {
										Scanner lineScanner = new Scanner(line);
										lineScanner.next();
										lineScanner.next();
										lineScanner.next();
										lineScanner.next();
										logger.error(LOGPREFIX + StringUtils.trim(lineScanner.nextLine()));
									}
								}
							}
						} else {
							scanner.next();
						}
					}
				}
			} catch (Exception e1) {
				throw new MojoExecutionException("", e1);
			}			
		}
	}	
}

