package latmod.ftbu.util.client;

import ftb.lib.client.FTBLibClient;
import latmod.ftbu.notification.Notification;
import latmod.ftbu.util.gui.GuiLM;
import latmod.ftbu.world.LMPlayerClient;
import latmod.lib.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.*;

@SideOnly(Side.CLIENT)
public class ClientNotifications
{
	private static Temp current = null;
	
	public static void renderTemp()
	{
		if(current != null)
		{
			current.render();
			if(current.isDead())
				current = null;
		}
		else if(!Temp.list.isEmpty())
		{
			current = Temp.list.get(0);
			Temp.list.remove(0);
		}
	}
	
	public static void add(Notification n)
	{
		if(n == null) return;
		if(n.ID != null)
		{
			Temp.list.removeObj(n.ID);
			Perm.list.removeObj(n.ID);
			if(current != null && current.ID != null && current.ID.equals(n.ID))
				current = null;
		}
		
		Temp.list.add(new Temp(n));
		if(!n.isTemp()) Perm.list.add(new Perm(n));
	}
	
	public static void init()
	{
		current = null;
		Perm.list.clear();
		Temp.list.clear();
	}
	
	public static class Temp extends Gui
	{
		public static final FastList<Temp> list = new FastList<Temp>();
		private static Minecraft mc;
		
		private long time;
		private String ID;
		private String title;
		private String desc;
		private double timer;
		private ItemStack item;
		private int color;
		private int width;

		public Temp(Notification n)
		{
			mc = FTBLibClient.mc;
			time = -1L;
			ID = n.ID;
			title = n.title.getFormattedText();
			desc = (n.desc == null) ? null : n.desc.getFormattedText();
			timer = (double)n.timer;
			item = n.item;
			color = LMColorUtils.getRGBA(n.color, 230);
			width = 20 + Math.max(mc.fontRendererObj.getStringWidth(title), mc.fontRendererObj.getStringWidth(desc));
			if(item != null) width += 20;
		}
		
		public String toString()
		{ return ID; }
		
		public boolean equals(Object o)
		{ return toString().equals(o.toString()); }
		
		public void render()
		{
			if(time == -1L) time = Minecraft.getSystemTime();
			
			if (time > 0L)
			{
				double d0 = (double)(Minecraft.getSystemTime() - time) / timer;
				
				if (d0 < 0D || d0 > 1D) { time = 0L; return; }
				
				double d1 = d0 * 2D;
				
				if (d1 > 1D) d1 = 2D - d1;
				d1 *= 4D;
				d1 = 1D - d1;

				if (d1 < 0D) d1 = 0D;
				
				d1 *= d1;
				d1 *= d1;
				
				int i = LatCoreMCClient.displayW - width;
				int j = 0 - (int)(d1 * 36D);
				int w = item == null ? 10 : 30;
				
				GlStateManager.pushAttrib();
				GlStateManager.disableDepth();
				GlStateManager.depthMask(false);
				GlStateManager.color(1F, 1F, 1F, 1F);
				GuiLM.drawBlankRect(i, j, 0D, LatCoreMCClient.displayW, j + 32, color);
				
				if(desc == null)
					mc.fontRendererObj.drawString(title, i + w, j + 12, -256);
				else
				{
					mc.fontRendererObj.drawString(title, i + w, j + 7, -256);
					mc.fontRendererObj.drawString(desc, i + w, j + 18, -1);
				}
				
				if(item != null) LMRenderHelper.renderGuiItem(item, mc.getRenderItem(), mc.fontRendererObj, i + 8, j + 8);
				
				GlStateManager.depthMask(true);
				GlStateManager.popAttrib();
			}
		}

		public boolean isDead()
		{ return time == 0L; }
	}
	
	public static class Perm implements Comparable<Perm>
	{
		public static final FastList<Perm> list = new FastList<Perm>();
		
		public final Notification notification;
		public final long timeAdded;
		
		public Perm(Notification n)
		{
			notification = n;
			timeAdded = LMUtils.millis();
		}
		
		public boolean equals(Object o)
		{ return notification.equals(o); }
		
		public int compareTo(Perm o)
		{ return Long.compare(timeAdded, o.timeAdded); }
		
		public void onClicked(LMPlayerClient p)
		{
			if(notification.mouse != null && notification.mouse.click != null)
				notification.mouse.click.onClicked(notification.mouse, p);
		}
	}
}