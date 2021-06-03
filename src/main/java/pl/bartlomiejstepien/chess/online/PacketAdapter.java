package pl.bartlomiejstepien.chess.online;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import pl.bartlomiejstepien.chess.online.packets.MovePacket;
import pl.bartlomiejstepien.chess.online.packets.Packet;

import java.lang.reflect.Type;

public class PacketAdapter implements JsonDeserializer<Packet>
{
    @Override
    public Packet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement jsonElement = jsonObject.get("packetType");
        String type = jsonElement.getAsString();
        if (Packet.PacketType.MOVE.name().equalsIgnoreCase(type))
        {
            return context.deserialize(json, MovePacket.class);
        }
        return null;
    }
}
