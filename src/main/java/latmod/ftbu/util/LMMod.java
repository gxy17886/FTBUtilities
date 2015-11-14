package latmod.ftbu.util;
import java.lang.annotation.*;
import java.lang.reflect.Field;

import org.apache.logging.log4j.*;

import ftb.lib.mod.FTBLibFinals;
import latmod.ftbu.mod.FTBU;
import latmod.ftbu.recipes.LMRecipes;
import latmod.lib.FastMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.relauncher.*;

public class LMMod
{
	public static final FastMap<String, LMMod> modsMap = new FastMap<String, LMMod>();
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Instance
	{
		public String value();
	}
	
	private static LMMod getLMMod(Object o)
	{
		if(o == null) return null;
		
		try
		{
			Field[] fields = o.getClass().getDeclaredFields();
			
			for(Field f : fields)
			{
				if(f.isAnnotationPresent(LMMod.Instance.class))
				{
					LMMod.Instance m = f.getAnnotation(LMMod.Instance.class);
					
					if(m.value() != null)
					{
						LMMod mod = new LMMod(m.value());
						f.set(o, mod);
						return mod;
					}
				}
			}
		}
		catch(Exception e)
		{ e.printStackTrace(); }
		
		return null;
	}
	
	public static void init(Object o)
	{
		LMMod mod = getLMMod(o);
		if(mod == null) { FTBU.mod.logger.warn("LMMod failed to load from " + o); return; }
		modsMap.put(mod.modID, mod);
		if(FTBLibFinals.DEV) FTBU.mod.logger.info("LMMod '" + mod.toString() + "' loaded");
	}
	
	// End of static //
	
	public final String modID;
	public final String lowerCaseModID;
	public final ModContainer modContainer;
	public final String assets;
	
	public Logger logger;
	public LMRecipes recipes;
	
	public LMMod(String id)
	{
		modID = id;
		modContainer = Loader.instance().getIndexedModList().get(modID);
		lowerCaseModID = modID.toLowerCase();
		assets = lowerCaseModID + ":";
		
		logger = LogManager.getLogger(modID);
		recipes = new LMRecipes();
	}
	
	public void setRecipes(LMRecipes r)
	{ recipes = (r == null) ? new LMRecipes() : r; }
	
	public String toFullString()
	{ return modID + '-' + Loader.MC_VERSION + '-' + modContainer.getDisplayVersion(); }
	
	public String toString()
	{ return modID; }
	
	public ResourceLocation getLocation(String s)
	{ return new ResourceLocation(lowerCaseModID, s); }
	
	public CreativeTabs createTab(final String s, final ItemStack icon)
	{
		CreativeTabs tab = new CreativeTabs(assets + s)
		{
			@SideOnly(Side.CLIENT)
			public ItemStack getIconItemStack()
			{ return icon; }
			
			@SideOnly(Side.CLIENT)
			public Item getTabIconItem()
			{ return getIconItemStack().getItem(); }
		};
		
		return tab;
	}
	
	public String getBlockName(String s)
	{ return assets + "tile." + s; }
	
	public String getItemName(String s)
	{ return assets + "item." + s; }
	
	@SideOnly(Side.CLIENT)
	public String translateClient(String s, Object... args)
	{ return I18n.format(assets + s, args); }
	
	public void addEntity(Class<? extends Entity> c, String s, int id)
	{ LatCoreMC.addEntity(c, s, id, modID); }
	
	public void onPostLoaded()
	{
	}
	
	public void loadRecipes()
	{
		if(recipes != null) recipes.loadRecipes();
	}
}