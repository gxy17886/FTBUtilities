package latmod.ftbu.mod.client.gui.guide;

import ftb.lib.client.*;
import ftb.lib.gui.GuiLM;
import ftb.lib.gui.widgets.*;
import ftb.lib.mod.client.gui.GuiViewImage;
import latmod.ftbu.api.guide.*;
import latmod.ftbu.mod.FTBU;
import latmod.ftbu.mod.client.FTBUClient;
import latmod.lib.LMUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;

public class GuiGuide extends GuiLM
{
	public static final ResourceLocation tex = FTBU.mod.getLocation("textures/gui/guide.png");
	public static final TextureCoords tex_slider = new TextureCoords(tex, 0, 240, 12, 18, 512, 512);
	public static final TextureCoords tex_back = new TextureCoords(tex, 0, 260, 15, 11, 512, 512);
	public static final TextureCoords tex_close = new TextureCoords(tex, 0, 271, tex_back.width, tex_back.height, 512, 512);
	
	public static final int textColor = 0xFF7B6534;
	public static final int textColorOver = 0xFF9D6A00;
	public static final int maxCategoryButtons = 15;
	public static final int maxTextLines = 20;
	
	public final GuiGuide parentGui;
	public final GuideCategory category;
	public GuideCategory selectedCategory;
	
	public final SliderLM sliderCategories, sliderText;
	public final ButtonLM buttonBack;
	
	public final WidgetLM categoriesPanel, textPanel;
	public final List<TextLine> allTextLines;
	public final List<ButtonCategory> categoryButtons; // Max 16
	public final ButtonTextLine[] textLines; // Max 20
	
	public static GuiGuide clientGuideGui = null;
	
	public static void openClientGui()
	{
		if(clientGuideGui == null) clientGuideGui = new GuiGuide(null, ClientGuideFile.instance.main);
		FTBLibClient.mc.displayGuiScreen(clientGuideGui);
	}
	
	public GuiGuide(GuiGuide g, GuideCategory c)
	{
		super(null, tex);
		parentGui = g;
		c.cleanup();
		category = c;
		selectedCategory = category;
		
		hideNEI = true;
		xSize = 328;
		ySize = 240;
		
		sliderCategories = new SliderLM(this, 11, 14, tex_slider.widthI(), 210, tex_slider.heightI())
		{
			public boolean canMouseScroll()
			{ return gui.mouseX < guiLeft + xSize / 2; }
			
			public boolean isEnabled()
			{ return category.subcategories.size() > maxCategoryButtons; }
		};
		
		sliderCategories.isVertical = true;
		
		sliderText = new SliderLM(this, 304, 14, tex_slider.widthI(), 210, tex_slider.heightI())
		{
			public boolean canMouseScroll()
			{ return gui.mouseX > guiLeft + xSize / 2; }
			
			public boolean isEnabled()
			{ return allTextLines.size() > maxTextLines; }
		};
		
		sliderText.isVertical = true;
		
		buttonBack = new ButtonLM(this, 35, 12, tex_back.widthI(), tex_back.heightI())
		{
			public void onButtonPressed(int b)
			{
				playClickSound();
				
				if(selectedCategory == category || category.getFormattedText().isEmpty())
				{
					if(parentGui == null) mc.thePlayer.closeScreen();
					else
					{
						parentGui.selectedCategory = parentGui.category;
						parentGui.sliderText.value = 0F;
						mc.displayGuiScreen(parentGui);
					}
				}
				else
				{
					selectedCategory = category;
					sliderText.value = 0F;
					if(parentGui != null) parentGui.refreshText();
					initLMGui();
				}
			}
		};
		
		categoriesPanel = new WidgetLM(this, 33, 29, 128, 200);
		textPanel = new PanelLM(this, 167, 10, 128, 219)
		{
			public void addWidgets()
			{ addAll(textLines); }
		};
		
		allTextLines = new ArrayList<>();
		categoryButtons = new ArrayList<>();
		textLines = new ButtonTextLine[maxTextLines];
		
		for(int i = 0; i < maxTextLines; i++)
			textLines[i] = new ButtonTextLine(this, i);
	}
	
	public void addWidgets()
	{
		mainPanel.add(sliderCategories);
		mainPanel.add(sliderText);
		mainPanel.add(buttonBack);
		mainPanel.add(textPanel);
		
		categoryButtons.clear();
		int catl = category.subcategories.size();
		int off = 0;
		
		if(catl > maxCategoryButtons)
		{
			float f = sliderCategories.value * (catl - 1 - maxCategoryButtons);
			off = (int) f;
			sliderCategories.scrollStep = 1F / (catl - 1 - maxCategoryButtons);
		}
		
		for(int i = 0; i < maxCategoryButtons; i++)
		{
			if(i + off < catl)
				categoryButtons.add(new ButtonCategory(GuiGuide.this, categoriesPanel.posX, categoriesPanel.posY + i * 13, categoriesPanel.width, 13, category.subcategories.get(i + off)));
		}
		
		mainPanel.addAll(categoryButtons);
	}
	
	@SuppressWarnings("unchecked")
	public void initLMGui()
	{
		if(category.getParentTop() == ClientGuideFile.instance.main) clientGuideGui = this;
		
		allTextLines.clear();
		
		GuideFile file = selectedCategory.getFile();
		if(file == null) return;
		
		String s = selectedCategory.getFormattedText();
		if(s != null && s.length() > 0)
		{
			boolean uni = fontRendererObj.getUnicodeFlag();
			fontRendererObj.setUnicodeFlag(FTBUClient.guide_unicode.get());
			List<String> list = fontRendererObj.listFormattedStringToWidth(s.trim(), textPanel.width);
			
			for(int i = 0; i < list.size(); i++)
			{
				TextLine l = new TextLine(null);
				l.text = list.get(i);
				l.special = file.getGuideLink(l.text);
				allTextLines.add(l);
				
				if(l.special != null)
				{
					if(l.special.type.isText())
					{
						l.text = (l.special.title == null) ? "" : l.special.title.getFormattedText();
						List<String> list1 = fontRendererObj.listFormattedStringToWidth(l.text, textPanel.width);
						
						if(list1.size() > 1)
						{
							l.text = list1.get(0);
							for(int j = 1; j < list1.size(); j++)
							{
								TextLine l1 = new TextLine(l);
								l1.text = list1.get(j);
								l1.special = l.special;
								allTextLines.add(l1);
							}
						}
					}
					else if(l.special.type.isImage())
					{
						try
						{
							TextureCoords tex = l.special.getTexture();
							
							if(tex.isValid())
							{
								l.text = "";
								int lines = (int) (1D + tex.getHeight(Math.min(textPanel.width, tex.width)) / 11D);
								
								TextureCoords[] splitTex = tex.split(1, lines);
								
								l.texture = splitTex[0];
								
								for(int j = 1; j < lines; j++)
								{
									TextLine l1 = new TextLine(l);
									l1.text = "";
									l1.special = l.special;
									l1.texture = splitTex[j];
									allTextLines.add(l1);
								}
							}
						}
						catch(Exception e1)
						{
							e1.printStackTrace();
						}
					}
				}
				
				if(!l.text.isEmpty()) l.text = l.text.replace('\ufffd', '\u00a7');
			}
			
			fontRendererObj.setUnicodeFlag(uni);
		}
		
		refreshText();
	}
	
	public void refreshText()
	{
		for(int i = 0; i < textLines.length; i++)
			textLines[i].line = null;
		
		int lines = allTextLines.size();
		int off = 0;
		
		if(lines > maxTextLines)
		{
			float f = sliderText.value * (lines - maxTextLines);
			off = (int) f;
			sliderText.scrollStep = 1F / (lines - maxTextLines);
		}
		
		for(int i = 0; i < maxTextLines; i++)
		{
			if(i + off < lines) textLines[i].line = allTextLines.get(i + off);
		}
	}
	
	public void drawTexturedModalRectD(double x, double y, double u, double v, double w, double h)
	{ drawTexturedModalRectD(x, y, zLevel, u, v, w, h, 512, 512); }
	
	public void drawBackground()
	{
		if(sliderCategories.isEnabled() && sliderCategories.update()) refreshWidgets();
		
		if(sliderText.isEnabled() && sliderText.update()) refreshText();
		
		super.drawBackground();
		
		if(sliderCategories.isEnabled()) sliderCategories.renderSlider(tex_slider);
		if(sliderText.isEnabled()) sliderText.renderSlider(tex_slider);
		
		FTBLibClient.setGLColor(buttonBack.mouseOver() ? textColorOver : textColor, 255);
		buttonBack.render((parentGui == null) ? tex_close : tex_back);
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		getFontRenderer();
		
		fontRendererObj.drawString(category.getTitleComponent().getFormattedText(), getPosX(53), getPosY(14), textColor);
		
		boolean uni = fontRendererObj.getUnicodeFlag();
		fontRendererObj.setUnicodeFlag(FTBUClient.guide_unicode.get());
		for(int i = 0; i < textLines.length; i++)
			textLines[i].renderWidget();
		fontRendererObj.setUnicodeFlag(uni);
		
		if(!categoryButtons.isEmpty())
		{
			for(int i = 0; i < categoryButtons.size(); i++)
				categoryButtons.get(i).renderWidget();
		}
	}
	
	public class ButtonCategory extends ButtonLM
	{
		public final GuideCategory cat;
		
		public ButtonCategory(GuiGuide g, int x, int y, int w, int h, GuideCategory c)
		{
			super(g, x, y, w, h);
			cat = c;
		}
		
		public void onButtonPressed(int b)
		{
			gui.playClickSound();
			
			if(cat.subcategories.isEmpty())
			{
				selectedCategory = cat;
				sliderText.value = 0F;
				initLMGui();
			}
			else gui.mc.displayGuiScreen(new GuiGuide(GuiGuide.this, cat));
		}
		
		public boolean isEnabled()
		{ return true; }
		
		public void renderWidget()
		{
			if(!isEnabled()) return;
			int ax = getAX();
			int ay = getAY();
			IChatComponent titleC = cat.getTitleComponent().createCopy();
			boolean mouseOver = mouseOver(ax, ay);
			if(mouseOver) titleC.getChatStyle().setUnderlined(true);
			if(selectedCategory == cat) titleC.getChatStyle().setBold(true);
			gui.getFontRenderer().drawString(titleC.getFormattedText(), ax + 1, ay + 1, mouseOver ? textColorOver : textColor);
		}
	}
	
	public class ButtonTextLine extends ButtonLM
	{
		public TextLine line = null;
		
		public ButtonTextLine(GuiGuide g, int i)
		{ super(g, 0, i * 11, g.textPanel.width, 11); }
		
		public void addMouseOverText(List<String> l)
		{
			if(line != null && line.special != null && line.special.hover != null)
			{
				String s = line.special.hover.getFormattedText();
				if(!s.isEmpty()) l.add(s);
			}
		}
		
		public void onButtonPressed(int b)
		{
			if(line == null || line.special == null) return;
			
			if(line.special.type == LinkType.URL)
			{
				try { LMUtils.openURI(new URI(line.special.link)); }
				catch(Exception e) { e.printStackTrace(); }
			}
			else if(line.special.type.isImage())
			{
				TextureCoords tc = line.special.getTexture();
				if(tc != null && tc.isValid()) mc.displayGuiScreen(new GuiViewImage(GuiGuide.this, tc));
			}
			else if(line.special.type == LinkType.RECIPE)
			{
				if(line.special.getItem() != null) NEIIntegration.openRecipe(line.special.getItem());
			}
		}
		
		public void renderWidget()
		{
			if(line == null) return;
			
			int ax = getAX();
			int ay = getAY();
			
			if(!line.text.isEmpty()) fontRendererObj.drawString(line.text, ax, ay, textColor);
			else if(line.special != null && line.special.type.isImage() && line.texture != null && line.texture.isValid())
			{
				GlStateManager.color(1F, 1F, 1F, 1F);
				gui.setTexture(line.texture.texture);
				double w = Math.min(width, line.texture.width);
				gui.render(line.texture, ax, ay, w, line.texture.getHeight(w) + 1);
				//GuiLM.drawTexturedRectD(ax, ay, gui.getZLevel(), w, line.texture.getHeight(w), 0D, line.texture.minU, 1D, line.texture.maxU);
			}
		}
	}
	
	private static class NEIIntegration
	{
		private static Boolean hasNEI = null;
		private static Method method = null;
		
		public static void openRecipe(ItemStack is)
		{
			if(is == null) return;
			
			if(hasNEI == null)
			{
				hasNEI = Boolean.FALSE;
				
				try
				{
					Class<?> c = Class.forName("codechicken.nei.recipe.GuiCraftingRecipe");
					method = c.getMethod("openRecipeGui", String.class, Object[].class);
					if(method != null) hasNEI = Boolean.TRUE;
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
			if(hasNEI.booleanValue())
			{
				try { method.invoke(null, "item", new Object[] {is}); }
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}