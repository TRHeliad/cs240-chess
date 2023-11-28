package chess;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ChessPieceAdapter extends TypeAdapter<ChessPiece> {
    @Override
    public void write(JsonWriter jsonWriter, ChessPiece chessPiece) throws IOException {
        Gson gson = new Gson();

        if (chessPiece == null) {
            jsonWriter.nullValue();
            return;
        }

        switch(chessPiece.getPieceType()) {
            case ROOK -> gson.getAdapter(Rook.class).write(jsonWriter, (Rook) chessPiece);
            case BISHOP -> gson.getAdapter(Bishop.class).write(jsonWriter, (Bishop) chessPiece);
            case KING -> gson.getAdapter(King.class).write(jsonWriter, (King) chessPiece);
            case KNIGHT -> gson.getAdapter(Knight.class).write(jsonWriter, (Knight) chessPiece);
            case PAWN -> gson.getAdapter(Pawn.class).write(jsonWriter, (Pawn) chessPiece);
            case QUEEN -> gson.getAdapter(Queen.class).write(jsonWriter, (Queen) chessPiece);
        }
    }

    @Override
    public ChessPiece read(JsonReader jsonReader) throws IOException {
        ChessGame.TeamColor teamColor = null;
        ChessPiece.PieceType type = null;

        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }

        jsonReader.beginObject();

        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case "type" -> type = ChessPiece.PieceType.valueOf(jsonReader.nextString());
                case "teamColor" -> teamColor = ChessGame.TeamColor.valueOf(jsonReader.nextString());
            }
        }

        jsonReader.endObject();

        if(type == null) {
            return null;
        } else {
            return switch (type) {
                case ROOK -> new Rook(teamColor);
                case BISHOP -> new Bishop(teamColor);
                case KING -> new King(teamColor);
                case KNIGHT -> new Knight(teamColor);
                case PAWN -> new Pawn(teamColor);
                case QUEEN -> new Queen(teamColor);
            };
        }
    }
}
