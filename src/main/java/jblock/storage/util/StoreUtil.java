package jblock.storage.util;


import jblock.crypto.Sha256;
import jblock.utils.SerializeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

public class StoreUtil {
    public static String[] SplitKey(String key){
        String[] rel = null;
        if(key != null){
            rel = key.split("_", 3);
            if(rel.length != 3){
                rel = null;
            }
        }
        return rel;
    }

    public static void RecomputeMerkleValue(TreeMap<String, byte[]> LeafHashs, HashMap<Integer, ArrayList<byte[]>> MerkleTree, Integer MaxGroup){
        ArrayList<byte[]> tmp = new ArrayList<>();
        if(LeafHashs != null && LeafHashs.size() > 0){
            //val tmparray = LeafHashs.values.toArray
            ArrayList<byte[]> tmparray = null;
            LeafHashs.values().forEach(value -> tmparray.add(value));
            //tmp ++= tmparray
            tmp.addAll(tmparray);

            ArrayList<byte[]> thesecondhash = getNextLevelList4Byte(tmp,MaxGroup);
            //MerkleTree += 1 -> thesecondhash
            MerkleTree.put(1, thesecondhash);
            Redo(MerkleTree,MaxGroup);
        }
    }

    private static int Redo(HashMap<Integer,ArrayList<byte[]>> MerkleTree,Integer MaxGroup){
        int rel = 0;
        int maxlevel = MerkleTree.size();
        ArrayList<byte[]> elems = MerkleTree.get(maxlevel);
        if(elems.size() > 1){
            int currentlevel = maxlevel + 1;
            //MerkleTree += currentlevel -> StoreUtil.getNextLevelList4Byte(elems,MaxGroup);
            MerkleTree.put(currentlevel, StoreUtil.getNextLevelList4Byte(elems,MaxGroup));
            Redo(MerkleTree,MaxGroup);
        }
        return rel;
    }

    /**
     * 合并两个byte[]数组
     * @ author daiyongbing
     * @param b1
     * @param b2
     * @return
     */
    public static byte[] concat(byte[] b1, byte[] b2){
        int len1 = b1.length;
        int len2 = b2.length;
        byte[] newbyte = new byte[len1+len2];
        int j =0;
        for (int i=0; i<newbyte.length; i++){
            if (i<len1){
                newbyte[i] = b1[i];
            }else {
                if (j<b2.length){
                    newbyte[i] = b2[j];
                    j++;
                }
            }
        }
        return newbyte;
    }

    public static ArrayList<byte[]>  getNextLevelList4Byte(ArrayList<byte[]> src,Integer MaxGroup){
        ArrayList<byte[]> nextlist = new ArrayList<>();
        int i = 0;
        while(i < src.size()){
            int j = 1;
            byte[] value = src.get(i);
            while(j < MaxGroup){
                i += 1;
                if(i < src.size()){
                    value = concat(value , src.get(i));
                }
                j += 1;
            }
            value = Sha256.hash(value);
            i+=1;
            nextlist.add(value);
        }
        return nextlist;
    }




    public static void UpdateNextLevelHash(ArrayList<byte[]> src,Integer UpdateIdx,Integer clevel, HashMap<Integer, ArrayList<byte[]>> ExistMerkle,Integer MaxGroup){
        int start = UpdateIdx / MaxGroup * MaxGroup;
        int tmpend = UpdateIdx / MaxGroup * MaxGroup + MaxGroup - 1;
        int end = tmpend;
        if(tmpend > src.size() -1){
            end = src.size() -1;
        }
        int UpdateNextIdx = UpdateIdx / MaxGroup;

       // var cmerkledata : mutable.ArrayBuffer[Array[Byte]] = ExistMerkle(clevel)
        ArrayList<byte[]> cmerkledata = ExistMerkle.get(clevel);  //留个错误,不能这样取

        int i = start;

        while(i < (end + 1)){
            int j = 1;
            byte[] value = src.get(i);
            while(j < MaxGroup){
                i += 1;

                if(i < (end + 1)){
                    value = concat(value , src.get(i));
                }
                j += 1;
            }
            value = Sha256.hash(value);
            i+=1;
            cmerkledata.set(UpdateNextIdx, value);
        }

        if(cmerkledata.size() > 1){
            UpdateNextLevelHash(cmerkledata,UpdateNextIdx,clevel+1,ExistMerkle,MaxGroup);
        }
    }

    public static void AddOrInsertNextLevelHash(ArrayList<byte[]> src, Integer StartPos, int clevel,
                                  HashMap<Integer, ArrayList<byte[]>> ExistMerkle, Integer MaxGroup){
        int start = StartPos / MaxGroup * MaxGroup;
        int end = src.size() -1;

        int UpdateNextIdx = StartPos / MaxGroup;

        ArrayList<byte[]> cmerkledata = null;
        if(ExistMerkle.containsKey(clevel)){
            cmerkledata = ExistMerkle.get(clevel);
        }else{
            cmerkledata  = new ArrayList<byte[]>();
            ExistMerkle.put(clevel, cmerkledata); // 什么鬼？
        }

        int dellen = cmerkledata.size() - UpdateNextIdx;
        if(dellen > 0){
            //cmerkledata.trimEnd(dellen);  //trimEnd(n: Int): Unit -> Removes the last n elements of this buffer.
            Iterator iterator = cmerkledata.iterator();
            final int size = cmerkledata.size();
            final int index = size-dellen;
            for (int i = size; i > index; i-- ){
                cmerkledata.remove(i-1);
            }
        }

        int i = start;
        while(i <= end ){
            int j = 1;
            byte[] value = src.get(i);
            while(j < MaxGroup){
                i += 1;
                if(i <= end){
                    value = concat(value , src.get(i));
                }
                j += 1;
            }
            value = Sha256.hash(value);
            cmerkledata.add(value);
            i+=1;
        }

        if(cmerkledata.size() > 1){
            AddOrInsertNextLevelHash(cmerkledata,UpdateNextIdx,clevel+1,ExistMerkle,MaxGroup);
        }else{
            int len = ExistMerkle.size();
            int j = len;
            while(j > clevel){
                if(ExistMerkle.containsKey(j)){
                    ExistMerkle.remove(j);
                }
                j -= 1;
            }
        }
    }

    public static void getUpdateNextLevelHash(ArrayList<byte[]> src, Integer StartIdx, Integer EndIdx, int clevel,
                                HashMap<Integer, ArrayList<byte[]>> ExistMerkle, Integer MaxGroup, int prevLength){
        int cStartIdx = StartIdx/MaxGroup/MaxGroup * MaxGroup;
        int cEndIdx = EndIdx/MaxGroup/MaxGroup * MaxGroup + MaxGroup -1;

        ArrayList<byte[]> cmerkledata = null;
        if(ExistMerkle.containsKey(clevel)){
            cmerkledata = ExistMerkle.get(clevel);
        }else{
            cmerkledata  = new ArrayList<byte[]>();
            //ExistMerkle += clevel -> cmerkledata;
            ExistMerkle.put(clevel, cmerkledata);
        }

        int currentLength = (prevLength -1)/MaxGroup + 1;
        if(cmerkledata.size() > currentLength){
            cmerkledata.remove(cmerkledata.size()-1);
        }

        int i = 0;
        int loop = StartIdx;
        while((i < src.size()) && (loop <= EndIdx)){
            int j = 1;
            byte[] value = src.get(i);
            while(j < MaxGroup){
                i += 1;
                loop += 1;
                if((i < src.size()) && (loop <= EndIdx)){
                    value = concat(value , src.get(i));
                }
                j += 1;
            }
            value = Sha256.hash(value);
            int tmpidx = loop/MaxGroup;
            if(tmpidx > cmerkledata.size()-1){
                //cmerkledata += value;
                cmerkledata.add(value);
            }else{
                //cmerkledata.update(tmpidx, value);
                cmerkledata.set(tmpidx, value);
            }

            i+=1;
            loop += 1;
        }

        if(cmerkledata.size() > 1){
            if(cEndIdx > (cmerkledata.size() - 1)){
                //创建cmerkledata的视图，[from, to)
                //val vs = cmerkledata.view(cStartIdx, cmerkledata.size());
                ArrayList<byte[]> vs = null;
                for (int index = cStartIdx; index < cEndIdx; index++){
                    vs.add(cmerkledata.get(index));
                }
                //val vsa = vs.toBuffer;
                //getUpdateNextLevelHash(vsa.asInstanceOf[ArrayBuffer[Array[Byte]]],cStartIdx,cmerkledata.length - 1,clevel+1,ExistMerkle,MaxGroup,cmerkledata.length);
                getUpdateNextLevelHash(vs,cStartIdx,cmerkledata.size() - 1,clevel+1,ExistMerkle,MaxGroup,cmerkledata.size());
            }else{
                ArrayList<byte[]> vs = null;
                for (int index = cStartIdx; index < cEndIdx+1; index++){
                    vs.add(cmerkledata.get(index));
                }
                /*val vs = cmerkledata.view(cStartIdx, cEndIdx+1);
                val vsa = vs.toBuffer;
                getUpdateNextLevelHash(vsa.asInstanceOf[ArrayBuffer[Array[Byte]]],cStartIdx,cEndIdx,clevel+1,ExistMerkle,MaxGroup,cmerkledata.length);*/
                getUpdateNextLevelHash(vs,cStartIdx,cEndIdx,clevel+1,ExistMerkle,MaxGroup,cmerkledata.size());
            }
        }else{
            int len = ExistMerkle.size();
            int k = len;
            while(k > clevel){
                if(ExistMerkle.containsKey(k)){
                    ExistMerkle.remove(k);
                }
                k -= 1;
            }
        }
    }

    public static byte[] ConvertLeafHashs2Bytes(TreeMap<String, byte[]> vobj){
        byte[] theLeafHashByByte = SerializeUtils.serialise(vobj);
        return theLeafHashByByte;
    }

    public static byte[] ConvertMerkleTree2Bytes(HashMap<Integer, ArrayList<byte[]>> vobj){
        byte[] theMerkleTreeByByte = SerializeUtils.serialise(vobj);
        return theMerkleTreeByByte;
    }
}
