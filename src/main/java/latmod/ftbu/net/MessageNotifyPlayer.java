package latmod.ftbu.net;
import latmod.ftbu.notification.Notification;
import latmod.ftbu.util.client.ClientNotifications;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.*;

public class MessageNotifyPlayer extends MessageFTBU
{
	public MessageNotifyPlayer() { super(DATA_SHORT); }
	
	public MessageNotifyPlayer(Notification n)
	{
		this();
		io.writeString(n.toJson());
	}
	
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageContext ctx)
	{
		ClientNotifications.add(Notification.fromJson(io.readString()));
		return null;
	}
}