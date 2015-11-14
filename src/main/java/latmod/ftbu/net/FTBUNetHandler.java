package latmod.ftbu.net;

import ftb.lib.api.LMNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class FTBUNetHandler
{
	static final LMNetworkWrapper NET = LMNetworkWrapper.newWrapper("FTBU");
	static final LMNetworkWrapper NET_INFO = LMNetworkWrapper.newWrapper("FTBUI");
	static final LMNetworkWrapper NET_WORLD = LMNetworkWrapper.newWrapper("FTBUW");
	
	public static void init()
	{
		NET.register(MessageLMWorldJoined.class, 1, Side.CLIENT);
		NET.register(MessageLMWorldUpdate.class, 2, Side.CLIENT);
		NET.register(MessageLMPlayerUpdate.class, 3, Side.CLIENT);
		NET.register(MessageLMPlayerLoggedIn.class, 4, Side.CLIENT);
		NET.register(MessageLMPlayerLoggedOut.class, 5, Side.CLIENT);
		NET.register(MessageLMPlayerDied.class, 6, Side.CLIENT);
		NET.register(MessageNotifyPlayer.class, 7, Side.CLIENT);
		NET.register(MessageClientAction.class, 8, Side.SERVER);
		
		NET_INFO.register(MessageLMPlayerInfo.class, 1, Side.CLIENT);
		NET_INFO.register(MessagePing.class, 2, Side.SERVER);
		NET_INFO.register(MessagePingResponse.class, 3, Side.CLIENT);
		NET_INFO.register(MessageDisplayGuide.class, 4, Side.CLIENT);
		
		NET_WORLD.register(MessageAreaUpdate.class, 1, Side.CLIENT);
		NET_WORLD.register(MessageAreaRequest.class, 2, Side.SERVER);
		NET_WORLD.register(MessageClaimChunk.class, 3, Side.SERVER);
	}
}