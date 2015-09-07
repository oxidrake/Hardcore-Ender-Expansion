package chylex.hee.system;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.StartupQuery;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import cpw.mods.fml.relauncher.Side;

public final class ModTransition{
	public static void runTransition(FMLMissingMappingsEvent e){
		final String id = "HardcoreEnderExpansion:";
		final Set<String> discard = new HashSet<>();
		
		discard.add(id+"transport_beacon");
		discard.add(id+"temple_end_portal");
		discard.add(id+"biome_compass");
		discard.add(id+"adventurers_diary");
		discard.add(id+"altar_nexus");
		discard.add(id+"temple_caller");
		
		e.get().stream().filter(mapping -> discard.contains(mapping.name)).forEach(MissingMapping::ignore);
		
		if (!shouldConvertWorld())return;
		
		boolean isClient = FMLCommonHandler.instance().getSide() == Side.CLIENT;
		
		StringBuilder build = new StringBuilder();
		build.append("\nHardcore Ender Expansion 2 needs to convert this world.\n\n");
		build.append("If you proceed, End Dimension and Ender Compendium\n");
		build.append("data will be "+EnumChatFormatting.BOLD+"deleted"+EnumChatFormatting.RESET+". ");
		if (isClient)build.append("If you logged out in the End,\nyou will be moved to your bed or spawn point.");
		else build.append("All players inside the End will\nbe moved to their bed or spawn point.");
		build.append("\n\nIt is suggested to make a "+EnumChatFormatting.BOLD+"backup"+EnumChatFormatting.RESET+" before confirming.\n\n");
		build.append("If you do not want to convert the world, please downgrade\n");
		build.append("to the last version of Hardcore Ender Expansion 1.");
		
		if (!StartupQuery.confirm(build.toString())){
			//Minecraft.getMinecraft().displayGuiScreen(new GuiSelectWorld(new GuiMainMenu()));
			//StartupQuery.abort();
			// TODO figure this out
		}
		else convertWorld();
	}
	
	private static boolean shouldConvertWorld(){
		File root = DimensionManager.getCurrentSaveRootDirectory();
		return !new File(root,"hee2").isDirectory() && new File(root,"DIM1").isDirectory();
	}
	
	private static void convertWorld(){
		File root = DimensionManager.getCurrentSaveRootDirectory();
		// TODO
	}
	
	private ModTransition(){}
}