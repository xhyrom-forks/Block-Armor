package twopiradians.blockArmor.common.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.command.CommandDev;
import twopiradians.blockArmor.common.config.Config;

@SuppressWarnings("deprecation")
public class ArmorSet {
	public static ArrayList<ArmorSet> allSets;
	/**Map of sets that have been auto generated and whether or not they are enabled in config*/
	public static HashMap<ArmorSet, Boolean> autoGeneratedSets = Maps.newHashMap();
	/**Map of sets that have effects and whether or not their effect is enabled*/
	public static HashMap<ArmorSet, Boolean> setsWithEffects = Maps.newHashMap();
	/**Used to add sets with effects or to add sets that would otherwise not be valid*/
	public static final ArrayList<ArmorSet> MANUALLY_ADDED_SETS;
	static {
		MANUALLY_ADDED_SETS = new ArrayList<ArmorSet>() {{
			add(new ArmorSet(new ItemStack(Blocks.BEDROCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.REDSTONE_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.LAPIS_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.EMERALD_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.OBSIDIAN, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.NETHERRACK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.SNOW, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.END_STONE, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.SLIME_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Items.REEDS, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.PRISMARINE, 1, 2), true));
			add(new ArmorSet(new ItemStack(Blocks.BRICK_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 0), true));

			add(new ArmorSet(new ItemStack(Blocks.BROWN_MUSHROOM_BLOCK, 1, 0), false));
			add(new ArmorSet(new ItemStack(Blocks.RED_MUSHROOM_BLOCK, 1, 0), false));
			add(new ArmorSet(new ItemStack(Blocks.CRAFTING_TABLE, 1, 0), false));
		}};
	}
	/**Armor set items that are missing textures that should be disabled*/
	public static ArrayList<ItemStack> disabledItems = new ArrayList<ItemStack>();

	public ItemStack stack;
	public Item item;
	public int meta;
	public Block block;
	public ArmorMaterial material;      
	public boolean hasSetEffect;
	public ItemBlockArmor helmet;
	public ItemBlockArmor chestplate;
	public ItemBlockArmor leggings;
	public ItemBlockArmor boots;
	public boolean isFromModdedBlock;

	@SideOnly(Side.CLIENT)
	public boolean isTranslucent;
	/**Array of block's textures sorted by EntityEquipmentSlot id*/
	@SideOnly(Side.CLIENT)
	private TextureAtlasSprite[] sprites;
	/**Array of TextureAtlasSprite's animation (or null if not animated) sorted by EntityEquipmentSlot id*/
	@SideOnly(Side.CLIENT)
	public AnimationMetadataSection[] animations;
	/**Array of TextureAtlasSprite's current frame number (including decimals between frames) sorted by EntityEquipmentSlot id*/
	@SideOnly(Side.CLIENT)
	public float[] frames;
	/**Array of quad's color (or -1 if none) sorted by EntityEquipmentSlot id*/
	@SideOnly(Side.CLIENT)
	private int[] colors;
	/**Minecraft's default missing texture sprite, assigned in initTextures()*/
	@SideOnly(Side.CLIENT)
	private static TextureAtlasSprite missingSprite;

	public ArmorSet(ItemStack stack, boolean hasSetEffect) {
		this.stack = stack;
		this.item = stack.getItem();
		try {
			ResourceLocation loc = (ResourceLocation)Item.REGISTRY.getNameForObject(this.item);
			if (!loc.getResourceDomain().equals("minecraft"))
				isFromModdedBlock = true;
		}
		catch (Exception e) {
			isFromModdedBlock = true;
		}
		this.meta = stack.getMetadata();
		if (item == Items.REEDS)
			this.block = Blocks.REEDS;
		else
			this.block = ((ItemBlock) item).getBlock();
		this.hasSetEffect = hasSetEffect;
		if (hasSetEffect)
			setsWithEffects.put(this, true);
		//calculate values for and set material
		float blockHardness = 0; 
		double durability = 5;
		float toughness = 0;
		int enchantability = 12;

		try {
			blockHardness = this.block.getBlockHardness(this.block.getDefaultState(), null, new BlockPos(0,0,0));
		} catch(Exception e) {
			blockHardness = ReflectionHelper.getPrivateValue(Block.class, this.block, 11); //blockHardness
		}
		if (blockHardness == -1) {
			durability = 0;
			blockHardness = 1000;
		}
		else
			durability = 2 + 8* Math.log(blockHardness + 1);
		if (blockHardness > 10)
			toughness = Math.min(blockHardness / 10F, 10);
		durability = Math.min(30, durability);
		blockHardness = (float) Math.log(blockHardness+1.5D)+1;
		int reductionHelmetBoots = (int) Math.min(Math.floor(Math.log10(Math.pow(blockHardness, 2)+1)+1.6D), 3);
		int reductionChest = (int) Math.min(blockHardness + 1, 8);
		int reductionLegs = (int) Math.max(reductionChest - 2, reductionHelmetBoots);
		int[] reductionAmounts = new int[] {reductionHelmetBoots, reductionLegs, reductionChest, reductionHelmetBoots};
		this.material = EnumHelper.addArmorMaterial(getItemStackDisplayName(stack, null)+" Material", "", 
				(int) durability, reductionAmounts, enchantability, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, toughness);
		this.material.repairMaterial = stack;
		//BlockArmor.logger.info(getItemStackDisplayName(stack, null)+": blockHardness = "+blockHardness+", toughness = "+toughness+", durability = "+durability);

		CommandDev.addBlockName(this);
	}

	/**Returns armor item for slot*/
	public ItemBlockArmor getArmorForSlot(EntityEquipmentSlot slot) {
		switch(slot) {
		case HEAD:
			return helmet;
		case CHEST:
			return chestplate;
		case LEGS:
			return leggings;
		case FEET:
			return boots;
		default:
			return null;
		}
	}

	/**Creates ArmorSets for each valid registered item and puts them in allSets*/
	public static void postInit() {
		//create list of all ItemStacks with different display names and list of the display names
		ArrayList<String> displayNames = new ArrayList<String>();
		Block[] blocks = Iterators.toArray(Block.REGISTRY.iterator(), Block.class);
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		for (Block block : blocks) {
			for (int i=0; i<16; i++)
				try {
					ItemStack stack = new ItemStack(block, 1, i);
					if (block.equals(Blocks.LOG) && i > 3) //logs after meta 3 are in log2
						break;
					if (stack != null && stack.getItem() != null && !stack.getDisplayName().equals("") && 
							!displayNames.contains(stack.getDisplayName())) {
						stacks.add(stack);
						displayNames.add(stack.getDisplayName());
					}
				} catch (Exception e) {continue;}
		}

		//creates list of names that the items will be registered with to prevent duplicates
		ArrayList<String> registryNames = new ArrayList<String>();
		for (ArmorSet set : MANUALLY_ADDED_SETS) 
			registryNames.add(getItemStackRegistryName(set.stack));

		//checks list of ItemStacks for valid ones and creates set and adds to allSets
		allSets = new ArrayList<ArmorSet>();
		allSets.addAll(MANUALLY_ADDED_SETS);
		for (ItemStack stack : stacks)
			if (isValid(stack) && ArmorSet.getSet(stack.getItem(), stack.getMetadata()) == null) {
				String registryName = getItemStackRegistryName(stack);
				if (!registryNames.contains(registryName) && !registryName.equals("")) {
					allSets.add(new ArmorSet(stack, false));
					registryNames.add(registryName);
					BlockArmor.logger.debug("Created ArmorSet for: "+stack.getDisplayName());
				}
			}

		//populate autoGeneratedSets
		for (ArmorSet set : allSets)
			if (!MANUALLY_ADDED_SETS.contains(set))
				autoGeneratedSets.put(set, true);
	}

	/**Returns TextureAtlasSprite corresponding to given ItemModArmor*/
	@SideOnly(Side.CLIENT)
	public static TextureAtlasSprite getSprite(ItemBlockArmor item) {		
		ArmorSet set = ArmorSet.getSet(item);
		if (set != null) {
			TextureAtlasSprite sprite = set.sprites[item.getEquipmentSlot().getIndex()];
			return sprite == null ? missingSprite : sprite;
		}
		else
			return missingSprite;
	}

	/**Returns current alpha for animation overlay corresponding to given ItemModArmor*/
	@SideOnly(Side.CLIENT)
	public static float getAlpha(ItemBlockArmor item) {
		ArmorSet set = ArmorSet.getSet(item);
		if (set != null) {
			float frame = set.frames[item.getEquipmentSlot().getIndex()];
			return frame - (int) frame;
		}
		else
			return 0;
	}

	/**Returns current animation frame corresponding to given ItemModArmor*/
	@SideOnly(Side.CLIENT)
	public static int getCurrentAnimationFrame(ItemBlockArmor item) {
		ArmorSet set = ArmorSet.getSet(item);
		AnimationMetadataSection animation;
		if (set != null && (animation = set.animations[item.getEquipmentSlot().getIndex()]) != null) {
			int frame = (int) set.frames[item.getEquipmentSlot().getIndex()];
			return animation.getFrameIndex(frame);
		}
		else
			return 0;
	}

	/**Returns next animation frame corresponding to given ItemModArmor*/
	@SideOnly(Side.CLIENT)
	public static int getNextAnimationFrame(ItemBlockArmor item) {
		ArmorSet set = ArmorSet.getSet(item);
		AnimationMetadataSection animation;
		if (set != null && (animation = set.animations[item.getEquipmentSlot().getIndex()]) != null) {
			int frame = (int) set.frames[item.getEquipmentSlot().getIndex()];
			if (frame++ >= animation.getFrameCount()-1)
				frame -= animation.getFrameCount();
			return animation.getFrameIndex(frame);
		}
		else
			return 0;
	}

	/**Returns color corresponding to given ItemModArmor*/
	@SideOnly(Side.CLIENT)
	public static int getColor(ItemBlockArmor item) {
		ArmorSet set = ArmorSet.getSet(item);
		if (set != null)
			return set.colors[item.getEquipmentSlot().getIndex()];
		else
			return -1;
	}

	/**Used to uniformly create registry name*/
	public static String getItemStackRegistryName(ItemStack stack) {
		try {
			String registryName = stack.getItem().getRegistryName().getResourcePath().toLowerCase().replace(" ", "_");
			registryName += (stack.getHasSubtypes() ? "_"+stack.getMetadata() : "");
			return registryName;
		} 
		catch (Exception e) {
			return "";
		}
	}

	/**Change display name based on the block*/
	public static String getItemStackDisplayName(ItemStack stack, EntityEquipmentSlot slot)	{
		String name;
		if (stack.getItem() instanceof ItemBlockArmor) {
			ArmorSet set = ArmorSet.getSet((ItemBlockArmor) stack.getItem());
			name = set.stack.getDisplayName();
		}
		else if (stack.getItem() != null)
			name = stack.getDisplayName();
		else
			name = "";

		//manually set display names
		name = name.replace("Block of ", "") 
				.replace("Block ", "")
				.replace(" Block", "")
				.replace("Sugar Canes", "Sugar Cane")
				.replace("Bricks", "Brick")
				.replace("Planks", "Plank");

		if (slot != null)
			switch (slot) {
			case HEAD:
				name += " Helmet";
				break;
			case CHEST:
				name += " Chestplate";
				break;
			case LEGS:
				name += " Leggings";
				break;
			case FEET:
				name += " Boots";
				break;
			default:
				break;
			}

		return name;
	}

	/**Determines if entity is wearing a full set of armor of same material*/
	public static boolean isWearingFullSet(EntityLivingBase entity, ArmorSet set)
	{
		if (entity != null && set != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST) != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS) != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.FEET) != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == set.boots
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == set.leggings
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == set.chestplate
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == set.helmet)
			return true;
		else
			return false;
	}

	/**Returns true if the set has a set effect and is enabled in Config*/
	public static boolean isSetEffectEnabled(ArmorSet set) {
		if (set == null || !set.hasSetEffect || Config.setEffects == 1)
			return false;
		if (setsWithEffects.get(set) || Config.setEffects == 0)
			return true;
		return false;
	}

	/**Returns armor set corresponding to given block and meta, or null if none exists*/
	public static ArmorSet getSet(Block block, int meta) {
		for (ArmorSet set : allSets)
			if (set.block == block && set.meta == meta)
				return set;
		return null;
	}

	/**Returns armor set corresponding to given item and meta, or null if none exists*/
	public static ArmorSet getSet(Item item, int meta) {
		for (ArmorSet set : allSets)
			if (set.item == item && set.meta == meta)
				return set;
		return null;
	}

	/**Returns armor set containing given ItemModArmor, or null if none exists*/
	public static ArmorSet getSet(ItemBlockArmor item) {
		for (ArmorSet set : allSets)
			if (set.helmet == item || set.chestplate == item || set.leggings == item || set.boots == item)
				return set;
		return null;
	}

	/**Should an armor set be made from this item*/
	private static boolean isValid(ItemStack stack) {
		try {
			if (stack == null || !(stack.getItem() instanceof ItemBlock) || 
					stack.getItem().getRegistryName().getResourcePath().contains("ore") || 
					stack.getDisplayName().contains(".name") || stack.getDisplayName().contains("Ore") ||
					stack.getDisplayName().contains("%"))
				return false;

			Block block = ((ItemBlock)stack.getItem()).getBlock();
			if (block instanceof BlockLiquid || block instanceof BlockContainer || block.hasTileEntity() || 
					block instanceof BlockOre || block instanceof BlockCrops || block instanceof BlockBush ||
					block == Blocks.BARRIER || block instanceof BlockLeaves || block == Blocks.MONSTER_EGG ||
					block instanceof BlockSlab || block.getRenderType(block.getDefaultState()) != EnumBlockRenderType.MODEL ||
					block == Blocks.IRON_BLOCK || block == Blocks.GOLD_BLOCK || block == Blocks.DIAMOND_BLOCK)
				return false;

			String registryName = block.getRegistryName().toString();
			if (registryName.equalsIgnoreCase("evilcraft:darkBlock") || 
					registryName.equalsIgnoreCase("evilcraft:obscuredGlass") ||
					registryName.equalsIgnoreCase("evilcraft:hardenedBlood") ||
					registryName.equalsIgnoreCase("evilcraft:darkPowerGemBlock") ||
					registryName.equalsIgnoreCase("darkutils:filter") || 
					registryName.equalsIgnoreCase("darkutils:filter_inverted") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedanalyser") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedbot") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedmutationstation") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedinscriber") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedhydrophonic") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedresearch") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedpipe") ||
					registryName.equalsIgnoreCase("agriculturalrevolution:rustedironscaff") ||
					registryName.equalsIgnoreCase("tconstruct:clear_glass"))
				return false;

			//Check if full block
			ArrayList<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
			try {
				block.addCollisionBoxToList(block.getDefaultState(), null, new BlockPos(0,0,0), Block.FULL_BLOCK_AABB, list, null);
			} catch (Exception e) {
				return false;
			}
			if (list.size() != 1 || !list.get(0).equals(Block.FULL_BLOCK_AABB)) 
				return false;

			return true;
		}
		catch (Exception e) { return false; }
	}

	/**Initialize set's texture variable*/
	@SideOnly(Side.CLIENT)
	public int initTextures() {
		if (missingSprite == null)
			missingSprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();

		int numTextures = 0;
		this.sprites = new TextureAtlasSprite[EntityEquipmentSlot.values().length];
		this.animations = new AnimationMetadataSection[EntityEquipmentSlot.values().length];
		this.frames = new float[EntityEquipmentSlot.values().length];
		this.colors = new int[EntityEquipmentSlot.values().length];
		for (int i=0; i<colors.length; i++)
			this.colors[i] = -1;

		//Gets textures from item model's BakedQuads (textures for each side)
		List<BakedQuad> list = new ArrayList<BakedQuad>();
		try {
			ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

			//getting quads may throw exception if a mod's modeler doesn't obey @Nullable
			list.addAll(mesher.getItemModel(this.stack).getQuads(null, null, 0));
			for (EnumFacing facing : EnumFacing.VALUES)
				list.addAll(mesher.getItemModel(this.stack).getQuads(null, facing, 0));

			for (BakedQuad quad : list) {
				ResourceLocation loc1 = new ResourceLocation(quad.getSprite().getIconName());

				TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(loc1.toString());
				AnimationMetadataSection animation = (AnimationMetadataSection) (sprite.getFrameCount() > 1 ? ReflectionHelper.getPrivateValue(TextureAtlasSprite.class, sprite, 3) : null); //animationMetadata
				int color = quad.hasTintIndex() ? Minecraft.getMinecraft().getItemColors().getColorFromItemstack(this.stack, quad.getTintIndex()) : -1;

				if (sprite.getIconName().contains("overlay")) //overlays not supported by forge so we can't account for them
					continue;

				if (quad.getFace() == EnumFacing.UP) {
					if (sprite != missingSprite)
						numTextures++;
					this.sprites[EntityEquipmentSlot.HEAD.getIndex()] = sprite;
					this.animations[EntityEquipmentSlot.HEAD.getIndex()] = animation;
					this.colors[EntityEquipmentSlot.HEAD.getIndex()] = color;
				}
				else if (quad.getFace() == EnumFacing.NORTH) {
					if (sprite != missingSprite)
						numTextures++;
					this.sprites[EntityEquipmentSlot.CHEST.getIndex()] = sprite;
					this.animations[EntityEquipmentSlot.CHEST.getIndex()] = animation;
					this.colors[EntityEquipmentSlot.CHEST.getIndex()] = color;
				}
				else if (quad.getFace() == EnumFacing.SOUTH) {
					if (sprite != missingSprite)
						numTextures++;
					this.sprites[EntityEquipmentSlot.LEGS.getIndex()] = sprite;
					this.animations[EntityEquipmentSlot.LEGS.getIndex()] = animation;
					this.colors[EntityEquipmentSlot.LEGS.getIndex()] = color;
				}
				else if (quad.getFace() == EnumFacing.DOWN) {
					if (sprite != missingSprite)
						numTextures++;
					this.sprites[EntityEquipmentSlot.FEET.getIndex()] = sprite;
					this.animations[EntityEquipmentSlot.FEET.getIndex()] = animation;
					this.colors[EntityEquipmentSlot.FEET.getIndex()] = color;
				}
			}
		}
		catch (Exception e) {}

		//Check for block texture overrides - location must be registered in ClientProxy TextureStitchEvent.Pre
		ResourceLocation texture = new ResourceLocation(BlockArmor.MODID+":textures/items/"+item.getRegistryName().getResourcePath().toLowerCase().replace(" ", "_")+".png");
		try {
			Minecraft.getMinecraft().getResourceManager().getResource(texture); //does texture exist?
			texture = new ResourceLocation(texture.getResourceDomain(), texture.getResourcePath().replace("textures/", "").replace(".png", ""));
			TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString());
			this.sprites[EntityEquipmentSlot.HEAD.getIndex()] = sprite;
			this.animations[EntityEquipmentSlot.HEAD.getIndex()] = null;
			this.sprites[EntityEquipmentSlot.CHEST.getIndex()] = sprite;
			this.animations[EntityEquipmentSlot.CHEST.getIndex()] = null;
			this.sprites[EntityEquipmentSlot.LEGS.getIndex()] = sprite;
			this.animations[EntityEquipmentSlot.LEGS.getIndex()] = null;
			this.sprites[EntityEquipmentSlot.FEET.getIndex()] = sprite;
			this.animations[EntityEquipmentSlot.FEET.getIndex()] = null;
			BlockArmor.logger.debug("Override texture found at: "+texture.toString());
		} catch (Exception e) {}

		//If a sprite is missing, disable the set
		if (this.sprites[EntityEquipmentSlot.HEAD.getIndex()] == null || 
				this.sprites[EntityEquipmentSlot.CHEST.getIndex()] == null || 
				this.sprites[EntityEquipmentSlot.LEGS.getIndex()] == null || 
				this.sprites[EntityEquipmentSlot.FEET.getIndex()] == null ||
				this.sprites[EntityEquipmentSlot.HEAD.getIndex()] == missingSprite ||
				this.sprites[EntityEquipmentSlot.CHEST.getIndex()] == missingSprite ||
				this.sprites[EntityEquipmentSlot.LEGS.getIndex()] == missingSprite || 
				this.sprites[EntityEquipmentSlot.FEET.getIndex()] == missingSprite) {
			disabledItems.add(new ItemStack(this.helmet));
			disabledItems.add(new ItemStack(this.chestplate));
			disabledItems.add(new ItemStack(this.leggings));
			disabledItems.add(new ItemStack(this.boots));
		}

		this.isTranslucent = this.block.getBlockLayer() != BlockRenderLayer.SOLID && this.block != Blocks.REEDS;

		return numTextures;
	}

	/**Remove recipes for items in disabledItems and set their creative tab to null*/
	public static void disableItems() {
		if (disabledItems == null || disabledItems.isEmpty())
			return;

		int numDisabled = 0;

		for (ItemStack stack : disabledItems) {	
			//remove from creative tab
			stack.getItem().setCreativeTab(null);

			//remove from vanilla tab
			if (BlockArmor.vanillaTab != null && BlockArmor.vanillaTab.orderedStacks != null)
				for (ItemStack tabStack : BlockArmor.vanillaTab.orderedStacks)
					if (tabStack.getItem() == stack.getItem()) {
						BlockArmor.vanillaTab.orderedStacks.remove(tabStack);
						break;
					}

			//remove from modded tab
			if (BlockArmor.moddedTab != null && BlockArmor.moddedTab.orderedStacks != null)
				for (ItemStack tabStack : BlockArmor.moddedTab.orderedStacks)
					if (tabStack.getItem() == stack.getItem()) {
						BlockArmor.moddedTab.orderedStacks.remove(tabStack);
						break;
					}

			//remove recipe
			List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
			for (IRecipe recipe : recipes)
				if (recipe.getRecipeOutput() != null && recipe.getRecipeOutput().getItem() == stack.getItem()) {
					BlockArmor.logger.debug("Disabling item: "+stack.getDisplayName());
					recipes.remove(recipe);
					numDisabled++;
					break;
				}
		}

		if (numDisabled > 0)
			BlockArmor.logger.info("Disabled "+numDisabled+" items that are missing textures");
	}
}
