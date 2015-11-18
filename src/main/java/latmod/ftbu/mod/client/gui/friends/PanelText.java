package latmod.ftbu.mod.client.gui.friends;

import latmod.ftbu.util.gui.GuiLM;
import latmod.lib.FastList;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class PanelText extends PanelFriendsGui
{
	public final FastList<String> text;
	
	public PanelText(GuiFriends g)
	{
		super(g);
		text = new FastList<String>();
	}
	
	public FontRenderer getFont()
	{ return gui.getFontRenderer(); }
	
	public void addWidgets()
	{
		if(text.isEmpty()) return;
		FontRenderer fr = getFont();
		height = 4 + text.size() * (fr.FONT_HEIGHT + 3);
		width = 0;
		for(int i = 0; i < text.size(); i++)
			width = Math.max(width, fr.getStringWidth(text.get(i)) + 5);
	}
	
	public void renderWidget()
	{
		if(text.isEmpty()) return;
		FontRenderer fr = getFont();
		
		int ax = getAX();
		int ay = getAY();
		
		float z = gui.getZLevel();
		GuiLM.drawBlankRect(ax, ay, z, width, height, 0x33666666);
		
		GlStateManager.translate(0F, 0F, z);
		for(int i = 0; i < text.size(); i++)
			fr.drawString(text.get(i), ax + 4, ay + 4 + i * (fr.FONT_HEIGHT + 3), 0xFFFFFFFF);
		GlStateManager.translate(0F, 0F, -z);
	}
}