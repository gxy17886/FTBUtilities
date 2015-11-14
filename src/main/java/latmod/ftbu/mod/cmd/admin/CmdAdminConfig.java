package latmod.ftbu.mod.cmd.admin;

import ftb.lib.api.config.ConfigListRegistry;
import ftb.lib.mod.FTBLibMod;
import latmod.ftbu.cmd.*;
import latmod.ftbu.mod.FTBU;
import latmod.lib.*;
import latmod.lib.config.*;
import net.minecraft.command.*;
import net.minecraft.util.*;

public class CmdAdminConfig extends CommandLM
{
	public CmdAdminConfig(String s)
	{ super(s, CommandLevel.OP); }
	
	public String[] getTabStrings(ICommandSender ics, String args[], int i)
	{
		if(i == 0) return getTabS(ConfigListRegistry.instance.list);
		else if(i == 1)
		{
			ConfigList list = ConfigListRegistry.instance.list.getObj(args[0]);
			if(list != null) return getTabS(list.groups);
		}
		else if(i == 2)
		{
			ConfigList list = ConfigListRegistry.instance.list.getObj(args[0]);
			if(list != null)
			{
				ConfigGroup group = list.groups.getObj(args[1]);
				if(group != null) return getTabS(group.entries);
			}
		}
		
		return null;
	}
	
	private static String[] getTabS(FastList<?> l)
	{
		String[] s = new String[l.size()];
		for(int i = 0; i < s.length; i++)
			s[i] = l.get(i).toString();
		return s;
	}
	
	public IChatComponent onCommand(ICommandSender ics, String[] args) throws CommandException
	{
		checkArgs(args, 3); // file, group, entry, value...
		
		ConfigList list = ConfigListRegistry.instance.list.getObj(args[0]);
		
		boolean success = false;
		if(list != null)
		{
			ConfigGroup group = list.groups.getObj(args[1]);
			
			if(group != null)
			{
				ConfigEntry entry = group.entries.getObj(args[2]);
				
				if(entry != null)
				{
					success = true;
					
					if(args.length >= 4)
					{
						String json = LMStringUtils.unsplitSpaceUntilEnd(3, args);
						
						FTBU.mod.logger.info("Setting " + args[0] + " " + args[1] + " " + args[2] + " to " + json);
						
						try
						{
							entry.setJson(LMJsonUtils.getJsonElement(json));
							if(list.parentFile != null) list.parentFile.save();
						}
						catch(Exception ex)
						{
							ChatComponentText error = new ChatComponentText(ex.getMessage());
							error.getChatStyle().setColor(EnumChatFormatting.RED);
							return error;
						}
					}
					else return new ChatComponentText(entry.getJson().toString());
				}
			}
		}
		
		if(!success) return new ChatComponentText("Can't find config entry '" + args[0] + " " + args[1] + " " + args[2] + "'");
		
		FTBLibMod.reload(ics, true);
		return null;
	}
}