package fr.eriniumgroup.erinium_faction.common.network.codecs;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Codec personnalisé pour encoder/décoder des tableaux d'entiers
 */
public class IntArrayCodec {

    /**
     * StreamCodec pour int[]
     */
    public static final StreamCodec<ByteBuf, int[]> INT_ARRAY = new StreamCodec<>() {
        @Override
        public int[] decode(ByteBuf buf) {
            int length = buf.readInt();
            int[] array = new int[length];
            for (int i = 0; i < length; i++) {
                array[i] = buf.readInt();
            }
            return array;
        }

        @Override
        public void encode(ByteBuf buf, int[] array) {
            if (array == null) {
                buf.writeInt(0);
                return;
            }
            buf.writeInt(array.length);
            for (int value : array) {
                buf.writeInt(value);
            }
        }
    };
}
