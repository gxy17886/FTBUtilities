package latmod.ftbu.badges;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.*;

@SideOnly(Side.CLIENT)
public class BadgeEmpty extends Badge
{
	public BadgeEmpty()
	{ super("_empty_"); }
	
	public ResourceLocation getTexture()
	{ return null; }
}