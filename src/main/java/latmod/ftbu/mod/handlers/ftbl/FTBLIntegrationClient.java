package latmod.ftbu.mod.handlers.ftbl;

import ftb.lib.*;
import ftb.lib.api.*;
import ftb.lib.client.FTBLibClient;
import ftb.lib.notification.ClientNotifications;
import latmod.ftbu.api.EventLMWorldClient;
import latmod.ftbu.api.guide.ClientGuideFile;
import latmod.ftbu.badges.ClientBadges;
import latmod.ftbu.mod.client.gui.claims.ClaimedAreasClient;
import latmod.ftbu.world.*;
import latmod.lib.ByteIOStream;
import net.minecraftforge.fml.relauncher.*;

@SideOnly(Side.CLIENT)
public class FTBLIntegrationClient extends FTBLIntegrationCommon
{
	public void onReloadedClient(EventFTBReload e)
	{
		FTBLibClient.clearCachedData();
		ClientBadges.clear();
		ClientGuideFile.instance.reload(e);
	}
	
	public void onFTBWorldClient(EventFTBWorldClient e)
	{
		ClientNotifications.init();
		
		if(e.world == null)
		{
			ClaimedAreasClient.clear();
			new EventLMWorldClient(LMWorldClient.inst, true).post();
			LMWorldClient.inst = null;
		}
		else if(e.isFake)
		{
			LMWorldClient.inst = new LMWorldClient(0);
		}
	}
	
	public void readWorldData(ByteIOStream io)
	{
		LMWorldClient.inst = new LMWorldClient(io.readInt());
		LMWorldClient.inst.readDataFromNet(io, true);
		FTBLib.logger.info("Joined the server with PlayerID " + LMWorldClient.inst.clientPlayerID + " on world " + FTBWorld.client.getWorldIDS());
		new EventLMWorldClient(LMWorldClient.inst, false).post();
	}
	
	public boolean hasClientWorld()
	{ return LMWorldServer.inst != null && LMWorldClient.inst.clientPlayerID > 0 && LMWorldClient.inst.clientPlayer != null; }
}