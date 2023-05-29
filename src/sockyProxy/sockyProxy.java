package sockyProxy;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.BurpExtension;
import burp.api.montoya.extension.ExtensionUnloadingHandler;

public class sockyProxy implements BurpExtension {
	public MontoyaApi api;
	@Override
	public void initialize(MontoyaApi mapi) {
		this.api = mapi;
		GUI UI = new GUI(api);
		api.extension().setName("sockyProxy");
		UI.debugMessage("sockyProxy loaded.");
		UI.debugMessage("Is Python Installed: " + String.valueOf(Utils.isPythonInstalled()));
		if (!Utils.isPythonInstalled() && !Utils.isPython3Installed()) {
			UI.debugMessage("You must install Python3 on your host system (with aiohttp and websockets library installed) and ensure python3 is in your PATH environment variable");
			Utils.infoBox("You must install Python3 on your host system (with aiohttp and websockets library installed) and ensure python3 is in your PATH environment variable", "Python not installed");
			return;
		}
		UI.debugMessage("Is the python aiohttp library installed: " + String.valueOf(Utils.isAiohttpInstalled(api)));
		UI.debugMessage("Is the python websocket library installed: " + String.valueOf(Utils.isWebocketInstalled(api)));
		if (!Utils.isAiohttpInstalled(api) || !Utils.isWebocketInstalled(api)) {
			UI.debugMessage("You must install the python library 'aiohttp' and 'websocket'. Try executing 'pip install aiohttp websocket' from a command prompt and relaunch the extention.");
			Utils.infoBox("You must install the python library 'aiohttp' and 'websocket'. Try executing 'pip install aiohttp websocket' from a command prompt and relaunch the extention.", "Python libraries not installed");
			return;
		}
		api.userInterface().registerSuiteTab("sockyProxy", UI);
		wsCreateHandler exampleWebSocketCreationHandler = new wsCreateHandler(api,UI.stm);
		api.websockets().registerWebSocketCreatedHandler(exampleWebSocketCreationHandler);
		extUnload unload = new extUnload(api);
		api.extension().registerUnloadingHandler(unload);
	}
	
	
	class extUnload implements ExtensionUnloadingHandler {
		private MontoyaApi api;
		
		extUnload(MontoyaApi mapi) {
			this.api = mapi;
		}
		
		@Override
		public void extensionUnloaded() {
			if (Utils.ProcessLauncher.process != null) {
				this.api.logging().logToOutput("[+] Websocket Proxy Server Stopped");
				Utils.ProcessLauncher.process.destroy();				
			}
		}
		
	}

}
