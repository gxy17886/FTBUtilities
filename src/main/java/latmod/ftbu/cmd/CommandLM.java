package latmod.ftbu.cmd;

import java.util.*;

import ftb.lib.FTBLib;
import latmod.ftbu.world.*;
import latmod.lib.FastList;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;

public abstract class CommandLM extends CommandBase
{
	public final String commandName;
	public final CommandLevel level;
	public final FastList<String> aliases = new FastList<String>();
	
	public CommandLM(String s, CommandLevel l)
	{
		commandName = s;
		level = (l == null) ? CommandLevel.NONE : l;
	}
	
	public int getRequiredPermissionLevel()
	{ return level.requiredPermsLevel(); }
	
	public boolean canCommandSenderUse(ICommandSender ics)
	{ return level != CommandLevel.NONE && (level == CommandLevel.ALL || super.canCommandSenderUse(ics)); }
	
	public final String getName()
	{ return commandName; }
	
	public String getCommandUsage(ICommandSender ics)
	{ return "/" + commandName; }
	
	public final void execute(ICommandSender ics, String[] args) throws CommandException
	{
		if(args == null) args = new String[0];
		
		IChatComponent s = onCommand(ics, args);
		if(s != null) FTBLib.printChat(ics, s);
		onPostCommand(ics, args);
	}
	
	public List<String> getCommandAliases()
	{ return aliases.isEmpty() ? null : aliases; }
	
	public abstract IChatComponent onCommand(ICommandSender ics, String[] args) throws CommandException;
	
	public static IChatComponent error(IChatComponent c)
	{ c.getChatStyle().setColor(EnumChatFormatting.RED); return c; }
	
	public final void printHelpLine(ICommandSender ics, String args)
	{ FTBLib.printChat(ics, "/" + commandName + (args != null && args.length() > 0 ? (" " + args) : "")); }
	
	public void onPostCommand(ICommandSender ics, String[] args) {}
	
	@SuppressWarnings("all")
	public final List addTabCompletionOptions(ICommandSender ics, String[] args, BlockPos pos)
	{
		try
		{
			String[] s = getTabStrings(ics, args, args.length - 1);
			if(s != null && s.length > 0)
			{
				if(sortStrings(ics, args, args.length - 1)) Arrays.sort(s);
				return getListOfStringsMatchingLastWord(args, s);
			}
		}
		catch(CommandException e) { }
		
		return null;
	}
	
	public NameType getUsername(String[] args, int i)
	{ return NameType.NONE; }
	
	public static boolean isArg(String[] args, int i, String... s)
	{
		if(args != null && i >= 0 && i < args.length)
		{
			for(int j = 0; j < s.length; j++)
				if(args[i].equals(s[j])) return true;
		}
		
		return false;
	}
	
	public String[] getTabStrings(ICommandSender ics, String args[], int i) throws CommandException
	{ return getUsername(args, i).getUsernames(); }
	
	public boolean sortStrings(ICommandSender ics, String args[], int i)
	{ return getUsername(args, i) == NameType.NONE; }
	
	public static EntityPlayerMP getPlayer(ICommandSender ics, String s) throws PlayerNotFoundException
	{
		EntityPlayerMP ep = getLMPlayer(s).getPlayer();
		if(ep != null) return ep;
		throw new PlayerNotFoundException();
	}
	
	public static LMPlayerServer getLMPlayer(Object o) throws PlayerNotFoundException
	{
		LMPlayerServer p = LMWorldServer.inst.getPlayer(o);
		if(p == null) throw new PlayerNotFoundException();
		return p;
	}
	
	public static void checkArgs(String[] args, int i) throws CommandException
	{ if(args == null || args.length < i) throw new MissingArgsException(); }
	
	public static void checkArgsStrong(String[] args, int i) throws CommandException
	{ if(args == null || args.length != i) throw new MissingArgsException(); }
}