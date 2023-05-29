package sockyProxy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;
import burp.api.montoya.MontoyaApi;
import java.io.InputStream;

public final class Utils {
	private static MontoyaApi api;
	public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
	public final static boolean isPythonInstalled() {
        try {
            Process process;
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                process = new ProcessBuilder("cmd.exe", "/c", "python", "--version").start();
            } else {
                process = new ProcessBuilder("python", "--version").start();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            reader.close();

            return line != null && line.toLowerCase().contains("python");
        } catch (IOException e) {
            return false;
        }
    }
	public final static boolean isPython3Installed() {
        try {
            Process process;
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                process = new ProcessBuilder("cmd.exe", "/c", "python3", "--version").start();
            } else {
                process = new ProcessBuilder("python3", "--version").start();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            reader.close();

            return line != null && line.toLowerCase().contains("python");
        } catch (IOException e) {
            return false;
        }
    }

	 public final static String getPython() {
		 if (Utils.isPython3Installed()) {
			 return "python3";
		 } else if(Utils.isPythonInstalled()) {
			 return "python";
		 }
		 return "python";
	 }
	 public final static boolean isAiohttpInstalled(MontoyaApi api) {		
        try {
            Process process = Runtime.getRuntime().exec(Utils.getPython() +" -m pip freeze");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("aiohttp")) {
                    return true;
                }
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;		
	 }
	 
	 public final static boolean isWebocketInstalled(MontoyaApi api) {
		 try {
	         Process process = Runtime.getRuntime().exec(Utils.getPython() +" -m pip freeze");
	         BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	         String line;
	
	         while ((line = reader.readLine()) != null) {
	             if (line.toLowerCase().contains("websocket==")) {
	                 return true;
	             }
	         }
	
	         reader.close();
	     } catch (Exception e) {
	         e.printStackTrace();
	     }
	
	     return false;
	 }
	 
	 public final static void write2file() {
	        try (BufferedWriter writer = new BufferedWriter(new FileWriter("sockyProxyServer.py"))) {
	            writer.write("import argparse\n" +
	            		"from aiohttp import web\n" +
	            		"import aiohttp\n" +
	            		"import asyncio\n" +
	            		"import websockets\n" +
	            		"async def handle(request):\n" +
	            		"    if request.method == 'GET':\n" +
	            		"        return web.Response(text='Please use POST method instead of GET')\n" +
	            		"    if request.method == 'POST':\n" +
	            		"        payload = await request.text()\n" +
	            		"        async with websockets.connect(request.app['ws_url']) as websocket:\n" +
	            		"            await websocket.send(payload)\n" +
	            		"            try:\n" +
	            		"                ws_response = await asyncio.wait_for(websocket.recv(), timeout=5)\n" +
	            		"            except asyncio.TimeoutError:\n" +
	            		"                ws_response = 'No response received in 5 seconds'\n" +
	            		"        return web.Response(text=ws_response)\n" +
	            		"def parse_arguments():\n" +
	            		"    parser = argparse.ArgumentParser()\n" +
	            		"    parser.add_argument('ws_url', help='The WebSocket URL to connect to')\n" +
	            		"    parser.add_argument('port', type=int, help='The port number to start the HTTP server on')\n" +
	            		"    return parser.parse_args()\n" +
	            		"async def main():\n" +
	            		"    args = parse_arguments()\n" +
	            		"    app = web.Application()\n" +
	            		"    app['ws_url'] = args.ws_url\n" +
	            		"    app.router.add_route('*', '/', handle)\n" +
	            		"    runner = aiohttp.web.AppRunner(app)\n" +
	            		"    await runner.setup()\n" +
	            		"    site = aiohttp.web.TCPSite(runner, '0.0.0.0', args.port)\n" +
	            		"    await site.start()\n" +
	            		"    print(f'Serving on port {args.port}')\n" +
	            		"    while True:\n" +
	            		"        await asyncio.sleep(3600)\n" +
	            		"if __name__ == '__main__':\n" +
	            		"    asyncio.run(main())\n");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    public final static boolean fileExist(String filename) {
	        File file = new File(filename);
	        if (file.exists()) {
	            return true;
	        } else {
	            return false;
	        }
	    }
 
	 public class ProcessLauncher {
	     public static Process process;
	
	     public static void Launch(MontoyaApi api, String port, String url) {
	     	try {
	     		 url = url.replaceFirst("https", "wss");
	             url = url.replaceFirst("http", "ws");
	             ProcessBuilder pb = new ProcessBuilder(Utils.getPython(), "sockyProxyServer.py", url, port);
	             pb.redirectErrorStream(true);
	             process = pb.start();
	             Thread terminationThread = new Thread(() -> {
	                 try {
	                     process.waitFor();
	                 } catch (InterruptedException e) {
	                     e.printStackTrace();
	                 }
	             });
	             terminationThread.start();
	             Thread outputThread = new Thread(() -> {
	                 try (InputStream inputStream = process.getInputStream();
	                      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
	                     String line;
	                     while ((line = reader.readLine()) != null) {
	                         api.logging().logToOutput(line);
	                     }
	                 } catch (IOException e) {
	                     e.printStackTrace();
	                 }
	             });
	             outputThread.start();
	             
	             // To kill the process at any time, call the destroy() method
	             // process.destroy();
	         } catch (IOException e) {
	             e.printStackTrace();
	         }
	     }
	     public static String launchpyCheck(String file)  throws IOException {
	         ProcessBuilder processBuilder = new ProcessBuilder(file);
	         Process process = processBuilder.start();
	
	         // Read the output of the program
	         InputStream inputStream = process.getInputStream();
	         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	
	         StringBuilder output = new StringBuilder();
	         String line;
	         while ((line = reader.readLine()) != null) {
	             output.append(line).append(System.lineSeparator());
	         }
	
	         // Wait for the program to finish and get the exit value
	         try {
	             int exitCode = process.waitFor();
	             System.out.println("Program exited with code: " + exitCode);
	         } catch (InterruptedException e) {
	             e.printStackTrace();
	         }
	
	         return output.toString();
	     }
	 }

	 public final static void LaunchProxy(MontoyaApi api, String port, String url) {
	    if (!Utils.fileExist("sockyProxyServer.py")) {
	    	Utils.write2file();
	    }
	    api.logging().logToOutput("[+] Websocket Proxy Server Started on port (" + port + ") and is redirecting to " + url);
	    ProcessLauncher.Launch(api,port,url);	            
	 }
 


}
