package latmod.core.event;

import java.io.File;

import latmod.core.util.LatCore;

public class SaveLMDataEvent extends EventLM
{
	public final File latmodFolder;
	
	public SaveLMDataEvent(File f)
	{ latmodFolder = f; }
	
	public File getFile(String s)
	{ return LatCore.newFile(new File(latmodFolder, s)); }
}