/*
 *  Copyright 2011 Jose Sebastian Reguera Candal
 *
 *  This file is part of Tremor.
 *
 *  Tremor is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Tremor is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Tremor.  If not, see <http://www.gnu.org/licenses/>. 
 */
package jsrc.sim.transputer;

public class Processor {
	
	public Processor() {
		internalMemory = new short[INTERNAL_MEMORY_SIZE / BYTES_PER_WORD];
	}
	
	public void step() {
		short inst = getMem(Iptr);
		Oreg |= (inst & INST_DATA_MASK);
		switch (inst & INST_CODE_MASK) {
		case J_CODE:
			Iptr = byteIndex(nextInst(), Oreg);
			Oreg = 0;
			break;
		case LDLP_CODE:
			Creg = Breg;
			Breg = Areg;
			Areg = index(Wptr, Oreg);
			Oreg = 0;
			Iptr = nextInst();
			break;
		case PFIX_CODE:
			Oreg <<= 4;
			Iptr += 1;
			break;
		case LDNL_CODE:
			// PRE: Areg & byteselectmask == 0
			Areg = getMem(index(Areg, Oreg));
			Oreg = 0;
			Iptr = nextInst();
			break;
		case LDC_CODE:
			Creg = Breg;
			Breg = Areg;
			Areg = Oreg;
			Oreg = 0;
			Iptr = nextInst();
			break;
		case LDNLP_CODE:
			// PRE: Areg & byteselectmask == 0
			Areg = index(Areg, Oreg);
			Oreg = 0;
			Iptr = nextInst();
			break;
		case NFIX_CODE:
			Oreg = (short) (~Oreg << 4);
			Iptr += 1;
			break;
		case LDL_CODE:
			Creg = Breg;
			Breg = Areg;
			Areg = getWorkspace(Oreg);
			Oreg = 0;
			Iptr = nextInst();
			break;
		case ADC_CODE:
			Areg += Oreg; // TODO checked + can set ErrorFlag on overflow
			Oreg = 0;
			Iptr = nextInst();
			break;
		case CALL_CODE:
			setMem(index(Wptr, (short) -1), Creg); // Index Wptr' 3
			setMem(index(Wptr, (short) -2), Breg); // Index Wptr' 2
			setMem(index(Wptr, (short) -3), Areg); // Index Wptr' 1
			setMem(index(Wptr, (short) -4), Iptr); // Index Wptr' 0
			Areg = nextInst();
			Wptr = index(Wptr, (short) -4);
			Iptr = byteIndex(nextInst(), Oreg);
			Oreg = 0;
			break;
		case CJ_CODE:
			if (Areg == 0) {
				Iptr = byteIndex(nextInst(), Oreg);
			} else {
				Areg = Breg;
				Breg = Creg;
				// Creg = undefined
				Iptr = nextInst();
			}
			Oreg = 0;
			break;
		case AJW_CODE:
			Wptr = index(Wptr, Oreg);
			Oreg = 0;
			Iptr = nextInst();
			break;
		case EQC_CODE:
			Areg = (Areg == Oreg)? TRUE_VALUE : FALSE_VALUE;
			Oreg = 0;
			Iptr = nextInst();
			break;
		case STL_CODE:
			setWorkspace(Oreg, Areg);
			Areg = Breg;
			Breg = Creg;
			// Creg = undefined
			Oreg = 0;
			Iptr = nextInst();
			break;
		case STNL_CODE:
			// PRE: Areg & byteselectmask == 0
			setMem(index(Areg, Oreg), Breg);
			Areg = Creg;
			// Breg = undefined
			// Creg = undefined
			Oreg = 0;
			Iptr = nextInst();
			break;
		case OPR_CODE:
			operate();
			Oreg = 0;
			break;
		}
	}
	
	private void operate() {
		switch (Oreg) {
		case REV_CODE:
			short temp = Areg; 
			Areg = Breg;
			Breg = temp;
			Iptr = nextInst();
			break;
		case LB_CODE:
			Areg = getByteMem(Areg);
			Iptr = nextInst();
			break;
		case BSUB_CODE:
			Areg = byteIndex(Areg, Breg);
			Breg = Creg;
			// Creg = undefined
			Iptr = nextInst();
			break;
		case ENDP_CODE:
			break;
		case DIFF_CODE:
			Areg = (short) (Breg - Areg);
			Breg = Creg;
			// Creg = undefined
			Iptr = nextInst();
			break;
		case ADD_CODE:
			Areg += Breg; // TODO checked + can set ErrorFlag on overflow
			Breg = Creg;
			// Creg = undefined
			Iptr = nextInst();
			break;
		case GCALL_CODE:
			short temp1 = Areg;
			Areg = nextInst();
			Iptr = temp1;
			break;
		case IN_CODE:
			break;
		case PROD_CODE:
			Areg *= Breg; // TODO checked * can set ErrorFlag on overflow
			Breg = Creg;
			// Creg = undefined
			Iptr = nextInst();
			break;
		case GT_CODE:
			Areg = Breg > Areg ? TRUE_VALUE : FALSE_VALUE;
			Breg = Creg;
			// Creg = undefined
			Iptr = nextInst();
			break;
		case WSUB_CODE:
			Areg = index(Areg, Breg);
			Breg = Creg;
			// Creg = undefined
			Iptr = nextInst();
			break;
		case OUT_CODE:
			break;
		case SUB_CODE:
			Areg = (short) (Breg - Areg); // TODO checked - can set ErrorFlag on overflow
			Breg = Creg;
			// Creg = undefined
			Iptr = nextInst();
			break;
		case STARP_CODE:
		case OUTBYTE_CODE:
		case OUTWORD_CODE:
		case SETERR_CODE:
		case RESETCH_CODE:
		case CSUB0_CODE:
		case STOPP_CODE:
		case LADD_CODE:
		case STLB_CODE:
		case STHF_CODE:
		case NORM_CODE:
		case LDIV_CODE:
			break;
		case LDPI_CODE:
			Areg = byteIndex(nextInst(), Areg);
			Iptr = nextInst();
			break;
		case STLF_CODE:
			break;
		case XDBLE_CODE:
			Creg = Breg;
			Breg = (short) (Areg < 0 ? -1 : 0);
			Iptr = nextInst();
			break;
		case LDPRI_CODE:
		case REM_CODE:
			break;
		case RET_CODE:
			Iptr = getMem(index(Wptr, (short) 0));
			Wptr = index(Wptr, (short) 4);
			break;
		case LEND_CODE:
		case LDTIMER_CODE:
		case TESTERR_CODE:
		case TESTPRANAL_CODE:
		case TIN_CODE:
		case DIV_CODE:
		case DIST_CODE:
		case DISC_CODE:
		case DIS_CODE:
		case LMUL_CODE:
			break;
		case NOT_CODE:
			Areg = (short) ~Areg;
			Iptr = nextInst();
			break;
		case XOR_CODE:
			Areg ^= Breg;
			Breg = Creg;
			// Creg = undefined
			Iptr = nextInst();
			break;
		case BCNT_CODE:
			Areg *= BYTES_PER_WORD;
			Iptr = nextInst();
			break;
		case LSHR_CODE:
		case LSHL_CODE:
		case LSUM_CODE:
		case LSUB_CODE:
		case RUNP_CODE:
		case XWORD_CODE:
			break;
		case SB_CODE:
			setByteMem(Areg, (byte) (Breg & 0xFF));
			Areg = Creg;
			// Breg = undefined	
			// Creg = undefined
			Iptr = nextInst();
			break;
		case GAJW_CODE:
			// PRE: Areg & byteselectmask == 0
			short temp2 = Areg;
			Areg = Wptr;
			Wptr = temp2;
			Iptr = nextInst();
			break;
		case SAVEL_CODE:
		case SAVEH_CODE:
		case WCNT_CODE:
			break;
		case SHL_CODE:
			// PRE: Areg <unsigned wordlength
			Areg = (short) (Breg << Areg);
			Breg = Creg;
			// Creg = undefined	
			Iptr = nextInst();
			break;
		case SHR_CODE:
			// PRE: Areg <unsigned wordlength
			Areg = (short) (Breg >> Areg);
			Breg = Creg;
			// Creg = undefined
			Iptr = nextInst();
			break;
		case MINT_CODE:
		case ALT_CODE:
		case ALTWT_CODE:
		case ALTEND_CODE:
			break;
		case AND_CODE:
			Areg &= Breg;
			Breg = Creg;
			// Creg = undefined
			Iptr = nextInst();
			break;
		case ENBT_CODE:
		case ENBC_CODE:
		case ENBS_CODE:
		case MOVE_CODE:
		case OR_CODE:
			Areg |= Breg;
			Breg = Creg;
			// Creg = undefined
			Iptr = nextInst();
			break;
		case CSNGL_CODE:
		case CCNT1_CODE:
		case TALT_CODE:
		case LDIFF_CODE:
		case STHB_CODE:
		case TALTWT_CODE:
			break;
		case SUM_CODE:
			Areg += Breg;
			Breg = Creg;
			// Creg = undefined
			Iptr = nextInst();
			break;
		case MUL_CODE:
			Areg *= Breg; // TODO checked * can set ErrorFlag on overflow
			Breg = Creg;
			// Creg = undefined
			Iptr = nextInst();
			break;
		case STTIMER_CODE:
		case STOPERR_CODE:
		case CWORD_CODE:
			break;
		case CLRHALTERR_CODE:
			HaltOnErrorFlag = false;
			Iptr = nextInst();
			break;
		case SETHALTERR_CODE:
			HaltOnErrorFlag = true;
			Iptr = nextInst();
			break;
		case TESTHALTERR_CODE:
			Creg = Breg;
			Breg = Areg;
			Areg = HaltOnErrorFlag ? TRUE_VALUE : FALSE_VALUE;
			Iptr = nextInst();
			break;
		}
	}	
	
	public void reset() {
		Iptr = 0;
		Wptr = 0;
		Areg = 0;
		Breg = 0;
		Creg = 0;
		Oreg = 0;
		HaltOnErrorFlag = false;
	}

	private short nextInst() {
		return (short) (Iptr + 1);
	}
	
	private short index(short base, short offset) {
		return (short) (base + BYTES_PER_WORD * offset);
	}
	
	private short byteIndex(short base, short offset) {
		return (short) (base + offset);
	}
	
	private byte getByteMem(short address) {
		return 0; // TODO
	}

	private void setByteMem(short address, byte value) {
		// TODO
	}
	
	private short getMem(short address) {
		return internalMemory[(address + MostNeg) / BYTES_PER_WORD];
	}

	private void setMem(short address, short value) {
		internalMemory[(address + MostNeg) / BYTES_PER_WORD] = value;
	}

	private short getWorkspace(short address) {
		return 0; // TODO
	}
	
	private void setWorkspace(short address, short value) {
		// TODO
	}

	
	private short Iptr;
	private short Wptr;
	private short Areg;
	private short Breg;
	private short Creg;
	private short Oreg;
	
	private boolean HaltOnErrorFlag;
	
	private short[] internalMemory;
	
	private static final int J_CODE = 0x00;
	private static final int LDLP_CODE = 0x10;
	private static final int PFIX_CODE = 0x20;
	private static final int LDNL_CODE = 0x30;
	private static final int LDC_CODE = 0x40;
	private static final int LDNLP_CODE = 0x50;
	private static final int NFIX_CODE = 0x60;
	private static final int LDL_CODE = 0x70;
	private static final int ADC_CODE = 0x80;
	private static final int CALL_CODE = 0x90;
	private static final int CJ_CODE = 0xA0;
	private static final int AJW_CODE = 0xB0;
	private static final int EQC_CODE = 0xC0;
	private static final int STL_CODE = 0xD0;
	private static final int STNL_CODE = 0xE0;
	private static final int OPR_CODE = 0xF0;
	
	private static final int REV_CODE = 0x00;
	private static final int LB_CODE = 0x01;
	private static final int BSUB_CODE = 0x02;
	private static final int ENDP_CODE = 0x03;
	private static final int DIFF_CODE = 0x04;
	private static final int ADD_CODE = 0x05;
	private static final int GCALL_CODE = 0x06;
	private static final int IN_CODE = 0x07;
	private static final int PROD_CODE = 0x08;
	private static final int GT_CODE = 0x09;
	private static final int WSUB_CODE = 0x0A;
	private static final int OUT_CODE = 0x0B;
	private static final int SUB_CODE = 0x0C;
	private static final int STARP_CODE = 0x0D;
	private static final int OUTBYTE_CODE = 0x0E;
	private static final int OUTWORD_CODE = 0x0F;
	private static final int SETERR_CODE = 0x10;
	private static final int RESETCH_CODE = 0x12;
	private static final int CSUB0_CODE = 0x13;
	private static final int STOPP_CODE = 0x15;
	private static final int LADD_CODE = 0x16;
	private static final int STLB_CODE = 0x17;
	private static final int STHF_CODE = 0x18;
	private static final int NORM_CODE = 0x19;
	private static final int LDIV_CODE = 0x1A;
	private static final int LDPI_CODE = 0x1B;
	private static final int STLF_CODE = 0x1C;
	private static final int XDBLE_CODE = 0x1D;
	private static final int LDPRI_CODE = 0x1E;
	private static final int REM_CODE = 0x1F;
	private static final int RET_CODE = 0x20;
	private static final int LEND_CODE = 0x21;
	private static final int LDTIMER_CODE = 0x22;
	private static final int TESTERR_CODE = 0x29;
	private static final int TESTPRANAL_CODE = 0x2A;
	private static final int TIN_CODE = 0x2B;
	private static final int DIV_CODE = 0x2C;
	private static final int DIST_CODE = 0x2E;
	private static final int DISC_CODE = 0x2F;
	private static final int DIS_CODE = 0x30;
	private static final int LMUL_CODE = 0x31;
	private static final int NOT_CODE = 0x32;
	private static final int XOR_CODE = 0x33;
	private static final int BCNT_CODE = 0x34;
	private static final int LSHR_CODE = 0x35;
	private static final int LSHL_CODE = 0x36;
	private static final int LSUM_CODE = 0x37;
	private static final int LSUB_CODE = 0x38;
	private static final int RUNP_CODE = 0x39;
	private static final int XWORD_CODE = 0x3A;
	private static final int SB_CODE = 0x3B;
	private static final int GAJW_CODE = 0x3C;
	private static final int SAVEL_CODE = 0x3D;
	private static final int SAVEH_CODE = 0x3E;
	private static final int WCNT_CODE = 0x3F;
	private static final int SHL_CODE = 0x41;
	private static final int SHR_CODE = 0x40;
	private static final int MINT_CODE = 0x42;
	private static final int ALT_CODE = 0x43;
	private static final int ALTWT_CODE = 0x44;
	private static final int ALTEND_CODE = 0x45;
	private static final int AND_CODE = 0x46;
	private static final int ENBT_CODE = 0x47;
	private static final int ENBC_CODE = 0x48;
	private static final int ENBS_CODE = 0x49;
	private static final int MOVE_CODE = 0x4A;
	private static final int OR_CODE = 0x4B;
	private static final int CSNGL_CODE = 0x4C;
	private static final int CCNT1_CODE = 0x4D;
	private static final int TALT_CODE = 0x4E;
	private static final int LDIFF_CODE = 0x4F;
	private static final int STHB_CODE = 0x50;
	private static final int TALTWT_CODE = 0x51;
	private static final int SUM_CODE = 0x52;
	private static final int MUL_CODE = 0x53;
	private static final int STTIMER_CODE = 0x54;
	private static final int STOPERR_CODE = 0x55;
	private static final int CWORD_CODE = 0x56;
	private static final int CLRHALTERR_CODE = 0x57;
	private static final int SETHALTERR_CODE = 0x58;
	private static final int TESTHALTERR_CODE = 0x59;
			
	private static final short TRUE_VALUE = 1;
	private static final short FALSE_VALUE = 0;
	
	private static final short INST_DATA_MASK = 0x0F;
	private static final short INST_CODE_MASK = 0xF0;
	
	private static final int BYTES_PER_WORD = 2;
	private static final int INTERNAL_MEMORY_SIZE = 4096;
	private static final short MemStart = (short) 0x8024;
	private static final short MostNeg = (short) 0x8000;
	private static final short MostPos = 0x7FFF;

}
