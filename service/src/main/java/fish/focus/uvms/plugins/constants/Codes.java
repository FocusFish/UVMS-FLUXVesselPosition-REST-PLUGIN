/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package fish.focus.uvms.plugins.constants;

import fish.focus.schema.exchange.movement.v1.MovementTypeType;

public interface Codes {

    enum FLUXVesselIDType {
        CFR,
        EXT_MARK,
        IRCS,
        UVI,
        MMSI,
        UUID,
        NAME;
    }

    enum FLUXVesselPositionType {
        ENTRY("ENT"), EXIT("EXI"), MANUAL("MAN"), POS("POS");
        
        protected String internalCode;
        private FLUXVesselPositionType(String internalCode) {
            this.internalCode = internalCode;
        }
        
        public static String fromInternal(MovementTypeType movementType) {
            for (FLUXVesselPositionType type : FLUXVesselPositionType.values()) {
                if (movementType != null && type.internalCode.equals(movementType.toString())) {
                    return type.toString();
                }
            }
            return "POS";
        }
        
        public static MovementTypeType fromExternal(String codeTypeValue) {
            for (FLUXVesselPositionType type : FLUXVesselPositionType.values()) {
                if (type.toString().equals(codeTypeValue)) {
                    return MovementTypeType.fromValue(type.internalCode);
                }
            }
            return MovementTypeType.POS;
        }
    }
}
