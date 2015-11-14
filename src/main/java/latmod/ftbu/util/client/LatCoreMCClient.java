package latmod.ftbu.util.client;

import ftb.lib.FTBLib;
import ftb.lib.client.FTBLibClient;
import latmod.ftbu.api.client.callback.ClientTickCallback;
import latmod.ftbu.mod.client.FTBURenderHandler;
import latmod.ftbu.notification.Notification;
import latmod.ftbu.util.gui.IClientActionGui;
import latmod.ftbu.world.LMWorldClient;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.client.registry.*;

/** Made by LatvianModder */
public final class LatCoreMCClient // LatCoreMC // FTBLibClient
{
	public static int displayW, displayH;
	
	public static void addEntityRenderer(Class<? extends Entity> c, Render r)
	{ RenderingRegistry.registerEntityRenderingHandler(c, r); }
	
	public static void addTileRenderer(Class<? extends TileEntity> c, TileEntitySpecialRenderer r)
	{ ClientRegistry.bindTileEntitySpecialRenderer(c, r); }
	
	public static void addClientTickCallback(ClientTickCallback e)
	{ FTBURenderHandler.callbacks.add(e); }
	
	public static void notifyClient(String ID, Object text, int t)
	{ ClientNotifications.add(new Notification(ID, FTBLib.getChatComponent(text), t)); }
	
	public static void onGuiClientAction()
	{
		if(FTBLibClient.mc.currentScreen instanceof IClientActionGui)
			((IClientActionGui)FTBLibClient.mc.currentScreen).onClientDataChanged();
	}
	
	public static boolean isPlaying()
	{
		return FTBLibClient.mc.theWorld != null
		&& FTBLibClient.mc.thePlayer != null
		&& FTBLibClient.mc.thePlayer.worldObj != null
		&& LMWorldClient.inst != null
		&& LMWorldClient.inst.getClientPlayer() != null;
	}
	
	public static int getDim()
	{ return isPlaying() ? FTBLibClient.mc.thePlayer.worldObj.provider.getDimensionId() : 0; }
}