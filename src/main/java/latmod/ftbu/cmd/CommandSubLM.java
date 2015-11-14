package latmod.ftbu.cmd;

import latmod.ftbu.mod.FTBU;
import latmod.lib.*;
import net.minecraft.command.*;
import net.minecraft.util.*;

public class CommandSubLM extends CommandLM
{
	public final FastMap<String, CommandLM> subCommands;
	
	public CommandSubLM(String s, CommandLevel l)
	{
		super(s, l);
		subCommands = new FastMap<String, CommandLM>();
	}
	
	public void add(CommandLM c)
	{ subCommands.put(c.commandName, c); }
	
	public String[] getTabStrings(ICommandSender ics, String args[], int i) throws CommandException
	{
		if(i == 0) return subCommands.keys.toArray(new String[0]);
		
		CommandLM cmd = subCommands.get(args[0]);
		
		if(cmd != null)
		{
			String[] s = cmd.getTabStrings(ics, trimArgs(args), i - 1);
			if(s != null && s.length > 0) return s;
		}
		
		return super.getTabStrings(ics, args, i);
	}
	
	public NameType getUsername(String[] args, int i)
	{
		if(i > 0 && args.length > 1)
		{
			CommandLM cmd = subCommands.get(args[0]);
			if(cmd != null)
				return cmd.getUsername(trimArgs(args), i - 1);
		}
		
		return NameType.NONE;
	}
	
	public IChatComponent onCommand(ICommandSender ics, String[] args) throws CommandException
	{
		if(args == null || args.length == 0)
			return new ChatComponentText(LMStringUtils.strip(getTabStrings(ics, args, 0)));
		CommandLM cmd = subCommands.get(args[0]);
		if(cmd != null) return cmd.onCommand(ics, trimArgs(args));
		return new ChatComponentTranslation(FTBU.mod.assets + "cmd.invalid_sub", args[0]);
	}
	
	private static String[] trimArgs(String[] args)
	{
		if(args == null || args.length == 0) return new String[0];
		String[] args1 = new String[args.length - 1];
		for(int i = 0; i < args1.length; i++)
			args1[i] = args[i + 1];
		return args1;
	}
}