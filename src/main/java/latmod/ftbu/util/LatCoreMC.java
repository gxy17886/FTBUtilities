package latmod.ftbu.util;
import java.util.Random;

import ftb.lib.FTBLib;
import latmod.ftbu.api.guide.GuideFile;
import latmod.ftbu.mod.FTBUTicks;
import latmod.ftbu.net.*;
import latmod.ftbu.notification.Notification;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.*;

/** Made by LatvianModder */
public final class LatCoreMC // LatCoreMCClient
{
	public static final Random rand = new Random();
	
	// Registry methods //
	
	public static void addItem(Item i, String name)
	{ GameRegistry.registerItem(i, name); }
	
	public static void addBlock(Block b, Class<? extends ItemBlock> c, String name)
	{ GameRegistry.registerBlock(b, c, name); }
	
	public static void addBlock(Block b, String name)
	{ addBlock(b, ItemBlock.class, name); }
	
	public static void addTileEntity(Class<? extends TileEntity> c, String s, String... alt)
	{
		if(alt == null || alt.length == 0) GameRegistry.registerTileEntity(c, s);
		else GameRegistry.registerTileEntityWithAlternatives(c, s, alt);
	}
	
	public static void addEntity(Class<? extends Entity> c, String s, int id, Object mod)
	{ EntityRegistry.registerModEntity(c, s, id, mod, 50, 1, true); }
	
	public static int getNewEntityID()
	{ return EntityRegistry.findGlobalUniqueEntityId(); }
	
	public static void addWorldGenerator(IWorldGenerator i, int w)
	{ GameRegistry.registerWorldGenerator(i, w); }
	
	public static Fluid addFluid(Fluid f)
	{
		Fluid f1 = FluidRegistry.getFluid(f.getName());
		if(f1 != null) return f1;
		FluidRegistry.registerFluid(f);
		return f;
	}
	
	public static void notifyPlayer(EntityPlayerMP ep, Notification n)
	{ new MessageNotifyPlayer(n).sendTo(ep); }
	
	public static boolean isDedicatedServer()
	{ return FTBUTicks.isDediServer; }
	
	public static void displayGuide(EntityPlayerMP ep, GuideFile file) 
	{ if(FTBLib.isServer() && !(ep instanceof FakePlayer)) new MessageDisplayGuide(file).sendTo(ep); }
}