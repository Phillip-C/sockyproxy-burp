package sockyProxy;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.websocket.*;
import sockyProxy.GUI.sockyTableModel;

public class wsMessageHandler implements MessageHandler {
	private final MontoyaApi api;
	private final sockyTableModel tbModel;
    public wsMessageHandler(MontoyaApi api, sockyTableModel tbModel) {
        this.api = api;
        this.tbModel = tbModel;
    }
    
    @Override
    public TextMessageAction handleTextMessage(TextMessage textMessage) {
        if (textMessage.direction() == Direction.CLIENT_TO_SERVER) {
	        wSocketMessage wsm = new wSocketMessage();
	        wsm.setMessage(textMessage);
	        wsm.setTimestamp();
	        this.tbModel.add(wsm);
        }
        return TextMessageAction.continueWith(textMessage);
    }

    @Override
    public BinaryMessageAction handleBinaryMessage(BinaryMessage binaryMessage) {
        return BinaryMessageAction.continueWith(binaryMessage);
    }
}
