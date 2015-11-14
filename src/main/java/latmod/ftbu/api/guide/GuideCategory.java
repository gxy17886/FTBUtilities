package latmod.ftbu.api.guide;

import ftb.lib.LMNBTUtils;
import latmod.lib.FastList;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.relauncher.*;

public class GuideCategory implements Comparable<GuideCategory> // GuideFile
{
	public final GuideCategory parent;
	private IChatComponent title;
	private FastList<IChatComponent> text;
	public final FastList<GuideCategory> subcategories;
	GuideFile file = null;
	
	public GuideCategory(GuideCategory p, IChatComponent s)
	{
		parent = p;
		title = s;
		text = new FastList<IChatComponent>();
		subcategories = new FastList<GuideCategory>();
	}
	
	public GuideFile getFile()
	{
		if(file != null) return file;
		else return parent == null ? null : parent.getFile();
	}
	
	public void println(IChatComponent c)
	{ text.add(c); }
	
	public void println(String s)
	{ println(new ChatComponentText(s)); }
	
	public String getUnformattedText()
	{
		if(text.isEmpty()) return "";
		StringBuilder sb = new StringBuilder();
		int s = text.size();
		for(int i = 0; i < s; i++)
		{ sb.append(text.get(i).getUnformattedText());
		if(i != s - 1) sb.append('\n'); }
		return sb.toString();
	}
	
	@SideOnly(Side.CLIENT)
	public String getFormattedText()
	{
		if(text.isEmpty()) return "";
		StringBuilder sb = new StringBuilder();
		int s = text.size();
		for(int i = 0; i < s; i++)
		{ sb.append(text.get(i).getFormattedText());
		if(i != s - 1) sb.append('\n'); }
		return sb.toString();
	}
	
	public void addSub(GuideCategory c)
	{ subcategories.add(c); }
	
	public IChatComponent getTitleComponent()
	{ return title; }
	
	public String toString()
	{ return title.getUnformattedText(); }
	
	public boolean equals(Object o)
	{ return o != null && (o == this || toString().equals(o.toString())); }
	
	public GuideCategory getSub(IChatComponent s)
	{
		for(int i = 0; i < subcategories.size(); i++)
		{
			GuideCategory c = subcategories.get(i);
			if(c.title.equals(s)) return c;
		}
		
		GuideCategory c = new GuideCategory(this, s);
		subcategories.add(c);
		return c;
	}
	
	public int compareTo(GuideCategory o)
	{ return toString().compareToIgnoreCase(o.toString()); }

	public void clear()
	{
		text.clear();
		for(int i = 0; i < subcategories.size(); i++)
			subcategories.get(i).clear();
		subcategories.clear();
	}
	
	void writeToNBT(NBTTagCompound tag)
	{
		tag.setString("N", IChatComponent.Serializer.componentToJson(title));
		
		if(text.size() > 0)
		{
			NBTTagList list = new NBTTagList();
			for(int i = 0; i < text.size(); i++)
				list.appendTag(new NBTTagString(IChatComponent.Serializer.componentToJson(text.get(i))));
			tag.setTag("T", list);
		}
		
		if(!subcategories.isEmpty())
		{
			NBTTagList list = new NBTTagList();
			for(int i = 0; i < subcategories.size(); i++)
			{
				NBTTagCompound tag1 = new NBTTagCompound();
				subcategories.get(i).writeToNBT(tag1);
				list.appendTag(tag1);
			}
			
			tag.setTag("S", list);
		}
	}
	
	void readFromNBT(NBTTagCompound tag)
	{
		clear();
		
		title = IChatComponent.Serializer.jsonToComponent(tag.getString("N"));
		
		if(tag.hasKey("T"))
		{
			NBTTagList list = tag.getTagList("T", LMNBTUtils.STRING);
			for(int i = 0; i < list.tagCount(); i++)
				text.add(IChatComponent.Serializer.jsonToComponent(list.getStringTagAt(i)));
		}
		
		if(tag.hasKey("S"))
		{
			NBTTagList list = tag.getTagList("S", LMNBTUtils.MAP);
			
			for(int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound tag1 = list.getCompoundTagAt(i);
				GuideCategory c = new GuideCategory(this, null);
				c.readFromNBT(tag1);
				subcategories.add(c);
			}
		}
	}
}