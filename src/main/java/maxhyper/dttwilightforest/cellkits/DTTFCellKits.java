package maxhyper.dttwilightforest.cellkits;

import com.ferreusveritas.dynamictrees.api.cell.Cell;
import com.ferreusveritas.dynamictrees.api.cell.CellKit;
import com.ferreusveritas.dynamictrees.api.cell.CellNull;
import com.ferreusveritas.dynamictrees.api.cell.CellSolver;
import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.cell.CellKits;
import com.ferreusveritas.dynamictrees.cell.NormalCell;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import maxhyper.dttwilightforest.DynamicTreesTheTwilightForest;

public class DTTFCellKits {

    public static void register(final Registry<CellKit> registry) {
        registry.registerAll(DARKWOOD);
    }

    public static final CellKit DARKWOOD = new CellKit(DynamicTreesTheTwilightForest.location("darkwood")) {

        /** Typical branch with hydration 5 */
        private final Cell branchCell = new NormalCell(8);

        private final Cell[] darkOakLeafCells = {
                CellNull.NULL_CELL,
                new DarkwoodLeafCell(1),
                new DarkwoodLeafCell(2),
                new DarkwoodLeafCell(3),
                new DarkwoodLeafCell(4),
                new DarkwoodLeafCell(5),
                new DarkwoodLeafCell(6),
                new DarkwoodLeafCell(7)
        };

        private final CellKits.BasicSolver darkOakSolver = new CellKits.BasicSolver(new short[]{0x0817, 0x0726, 0x0715, 0x0615, 0x0514, 0x0413, 0x0322, 0x0221});

        @Override
        public Cell getCellForLeaves(int hydro) {
            return darkOakLeafCells[hydro];
        }

        @Override
        public Cell getCellForBranch(int radius, int meta) {
            return radius == 1 ? branchCell : CellNull.NULL_CELL;
        }

        @Override
        public SimpleVoxmap getLeafCluster() {
            return DTTFLeafClusters.DARKWOOD;
        }

        @Override
        public CellSolver getCellSolver() {
            return darkOakSolver;
        }

        @Override
        public int getDefaultHydration() {
            return 7;
        }

    };

}
