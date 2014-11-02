package chylex.hee.world.structure.island.biome.interaction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import chylex.hee.block.BlockList;
import chylex.hee.entity.mob.EntityMobHomelandEnderman;
import chylex.hee.entity.technical.EntityTechnicalBiomeInteraction;
import chylex.hee.mechanics.misc.HomelandEndermen;
import chylex.hee.mechanics.misc.HomelandEndermen.OvertakeGroupRole;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C08PlaySound;
import chylex.hee.system.util.MathUtil;
import chylex.hee.world.structure.island.biome.data.AbstractBiomeInteraction;

public class BiomeInteractionEnchantedIsland{
	public static class InteractionOvertake extends AbstractBiomeInteraction{
		public long groupId = -1L;
		private int overtakeTimer;
		
		@Override
		public void init(){
			List<EntityMobHomelandEnderman> endermen = world.getEntitiesWithinAABB(EntityMobHomelandEnderman.class,getIslandBoundingBox());
			//System.out.println("spawned overtake thing");
			
			for(int attempt = 0; attempt < 3 && !endermen.isEmpty(); attempt++){
				EntityMobHomelandEnderman subject = endermen.remove(rand.nextInt(endermen.size()));
				
				if (subject.getGroupId() != -1L){
					List<EntityMobHomelandEnderman> sameGroup = HomelandEndermen.getInSameGroup(subject);
					if (sameGroup.size() < 5)continue;
					
					List<OvertakeGroupRole> roles = new ArrayList<>(Arrays.asList(OvertakeGroupRole.values));
					for(EntityMobHomelandEnderman enderman:sameGroup)roles.remove(enderman.getGroupRole());
					
					if (roles.isEmpty() && rand.nextInt(666) < MathUtil.square(sameGroup.size()) && rand.nextInt(3) == 0){
						groupId = subject.getGroupId();
						
						//System.out.println("STARTING OVERTAKE WITH "+sameGroup.size()+" MEMBERS");
						//for(EntityMobHomelandEnderman e:sameGroup)System.out.println(e.getGroupRole());
						break;
					}
				}
			}
			
			if (groupId == -1L)entity.setDead();
		}

		@Override
		public void update(){
			if (++overtakeTimer > 500+rand.nextInt(300))entity.setDead();
		}

		@Override
		public void saveToNBT(NBTTagCompound nbt){
			nbt.setLong("group",groupId);
			nbt.setShort("timer",(short)overtakeTimer);
		}

		@Override
		public void loadFromNBT(NBTTagCompound nbt){
			groupId = nbt.getLong("group");
			overtakeTimer = nbt.getShort("timer");
		}
	}
	
	public static class InteractionCellarSounds extends AbstractBiomeInteraction{
		private enum Procedure{
			FOOTSTEPS, BLOCK_BREAKING, CHEST_OPENING;
			static final Procedure[] values = values();
		}
		
		private enum BreakEffects{
			GRASS, GRAVEL, SAND, WOOD;
			static final BreakEffects[] values = values();
		}
		
		private EntityPlayer target;
		private Procedure procedure;
		private byte lastSoundId, cellarCheckTimer;
		private int timer, waitTimer;
		private float soundAngle, soundDist;
		private boolean isActive = true;
		
		@Override
		public void init(){
			List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,getIslandBoundingBox());
			
			if (players.isEmpty()){
				entity.setDead();
				return;
			}
			
			List<EntityTechnicalBiomeInteraction> interactions = world.getEntitiesWithinAABB(EntityTechnicalBiomeInteraction.class,entity.boundingBox.expand(1D,1D,1D));
			
			for(int attempt = 0; attempt <= 10; attempt++){
				if (attempt == 10){
					target = null;
					entity.setDead();
					return;
				}
				
				if ((target = players.get(rand.nextInt(players.size()))).isDead)continue;
				
				for(EntityTechnicalBiomeInteraction interaction:interactions){
					if (interaction != entity && interaction.getInteractionType() == InteractionCellarSounds.class && ((InteractionCellarSounds)interaction.getInteraction()).target == target){
						target = null;
						break;
					}
				}
				
				if (target != null)break;
			}
			
			procedure = Procedure.values[rand.nextInt(Procedure.values.length)];
			
			soundDist = 36F+rand.nextFloat()*5F;
			soundAngle = (float)(rand.nextDouble()*Math.PI*2D);
		}

		@Override
		public void update(){
			if (target == null || target.isDead || !target.boundingBox.intersectsWith(getIslandBoundingBox())){
				entity.setDead();
				return;
			}
			
			if (rand.nextInt(10) == 0){
				soundAngle += (rand.nextBoolean() ? -1 : 1)*(0.5D+rand.nextDouble()*0.5D)*MathUtil.toRad(4D);
				System.out.println("new angle: "+soundAngle);
			}
			
			if (waitTimer > 0)--waitTimer;
			
			if (soundDist > 60D)entity.setDead();
			if (soundDist > 30D)return;
			
			if (++cellarCheckTimer > 10){
				boolean foundBottom = false, foundTop = false;
				int tx = (int)target.posX, tz = (int)target.posZ, minPersegritY = (int)target.posY, maxPersegritY = minPersegritY+1;
				
				for(int a = 0; a < 8; a++){
					if (!foundBottom){
						if (world.getBlock(tx,minPersegritY,tz) != BlockList.persegrit)--minPersegritY;
						else foundBottom = true;
					}
					
					if (!foundTop){
						if (world.getBlock(tx,maxPersegritY,tz) != BlockList.persegrit)++maxPersegritY;
						else foundTop = true;
					}
					
					if (foundBottom && foundTop)break;
				}
				
				if (foundBottom && foundTop){
					if (soundDist > 5D)soundDist -= 0.08D+rand.nextDouble()*0.02D;
					isActive = true;
				}
				else{
					soundDist += 0.3D;
					isActive = false;
				}
				
				System.out.println("new dist "+soundDist);
			}
			
			if (!isActive)return;
			
			if (--timer < 0){
				switch(procedure){
					case FOOTSTEPS:
						timer = 5+rand.nextInt(3);
						play(C08PlaySound.PERSEGRIT_FOOTSTEPS,BlockList.persegrit.stepSound.getVolume()*0.15F,BlockList.persegrit.stepSound.getPitch()*1F);
						break;
						
					case BLOCK_BREAKING:
						timer = 7+rand.nextInt(8+rand.nextInt(15));
						if (lastSoundId == 0 || rand.nextInt(8) == 0)lastSoundId = (byte)(1+rand.nextInt(BreakEffects.values.length));
						
						play((byte)(C08PlaySound.GRASS_BREAK-1+lastSoundId),1F,BlockList.persegrit.stepSound.getPitch()*0.8F);
						break;
						
					case CHEST_OPENING:
						if (lastSoundId == 0){
							timer = 12+rand.nextInt(10+rand.nextInt(30));
							lastSoundId = 1;
							play(C08PlaySound.CHEST_OPEN,0.5F,0.9F+rand.nextFloat()*0.1F);
						}
						else{
							timer = 3+rand.nextInt(4);
							lastSoundId = 0;
							play(C08PlaySound.CHEST_CLOSE,0.5F,0.9F+rand.nextFloat()*0.1F);
						}
						
						break;
				}
				
				if (--waitTimer < -8-rand.nextInt(12)){
					waitTimer = 10+(int)Math.floor(15F*soundDist*(0.3F+rand.nextFloat()*0.6F));
				}
			}
		}
		
		private void play(byte soundId, float volume, float pitch){
			System.out.println("played sound");
			PacketPipeline.sendToPlayer(target,new C08PlaySound(soundId,target.posX+MathHelper.cos(soundAngle)*soundDist,target.posY,target.posZ+MathHelper.sin(soundAngle)*soundDist,volume,pitch));
		}

		@Override
		public void saveToNBT(NBTTagCompound nbt){}

		@Override
		public void loadFromNBT(NBTTagCompound nbt){}
	}
}
