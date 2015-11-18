package latmod.ftbu.mod.cmd;

import javax.script.*;

import latmod.ftbu.cmd.*;
import latmod.lib.LMStringUtils;
import net.minecraft.command.*;
import net.minecraft.util.*;

public class CmdMath extends CommandLM
{
	private static ScriptEngine engine = null;
	private static boolean engineCached = false;
	
	public CmdMath()
	{ super("math", CommandLevel.ALL); }
	
	public IChatComponent onCommand(ICommandSender ics, String[] args) throws CommandException
	{
		checkArgs(args, 1);
		
		if(engine == null && !engineCached)
		{
			engineCached = true;
			
			try
			{
				ScriptEngineManager manager = new ScriptEngineManager();
				engine = manager.getEngineByName("JavaScript");
				if(engine == null) engine = manager.getEngineByName("Nashorn");
				if(engine == null) engine = manager.getEngineByName("Rhino");
			}
			catch(Exception e) { }
		}
		
		if(engine == null) return error(new ChatComponentText("No JS Engine found"));
		
		String s = LMStringUtils.unsplit(args, " ").trim();
		
		try
		{
			Object o = engine.eval(s);
			return new ChatComponentText(String.valueOf(o));
		}
		catch(Exception e) { e.printStackTrace(); }
		
		return error(new ChatComponentText("Error"));
	}
}