package maxhyper.dttwilightforest.trees;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import maxhyper.dttwilightforest.blocks.BasicRootsBlock;
import net.minecraft.resources.ResourceLocation;

public class MangroveFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(MangroveFamily::new);

    private BasicRootsBlock roots;

    public MangroveFamily(ResourceLocation name) {
        super(name);
    }

    public void setRoots(BasicRootsBlock roots) {
        this.roots = roots;
    }

    public BasicRootsBlock getRoots() {
        return roots;
    }

    //    @Override
//    public void setupBlocks() {
//        this.setRoots(this.createRoot(this.getBranchName()));
//        this.setRootsItem(this.createRootItem(this.getBranchName(), this.roots));
//        super.setupBlocks();
//    }
//    protected String getRootNameSuffix() {
//        return BasicRootsBlock.NAME_SUFFIX;
//    }
//    protected Supplier<BranchBlock> createRoot(final ResourceLocation name) {
//        return RegistryHandler.addBlock(suffix(name, getRootNameSuffix()), () -> createRootBlock(name));
//    }
//    public Supplier<BlockItem> createRootItem(final ResourceLocation registryName, final Supplier<BranchBlock> branchSup) {
//        return RegistryHandler.addItem(suffix(registryName, getRootNameSuffix()), () -> new BlockItem(branchSup.get(), new Item.Properties()));
//    }
//    protected BranchBlock createRootBlock(ResourceLocation name) {
//        return new BasicRootsBlock(name, this.getProperties());
//    }
//    protected Family setRoots(final Supplier<BranchBlock> rootSup) {
//        this.roots = this.setupBranch(rootSup, false);
//        return this;
//    }
//    @SuppressWarnings("unchecked")
//    protected <T extends Item> Family setRootsItem(Supplier<T> branchItemSup) {
//        this.rootsItem = (Supplier<Item>) branchItemSup;
//        return this;
//    }
//
//    public Optional<Item> getRootsItem() {
//        return Optionals.ofItem(this.rootsItem);
//    }
}
