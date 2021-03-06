package latmod.ftbu.net;

import ftb.lib.api.LMNetworkWrapper;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public class MessagePing extends MessageFTBU
{
	public MessagePing() { super(null); }
	
	public LMNetworkWrapper getWrapper()
	{ return FTBUNetHandler.NET_INFO; }
	
	public IMessage onMessage(MessageContext ctx)
	{ return new MessagePingResponse(); }
}