/*
 * Copyright (C) 2021 Christian Bernstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package de.christianbernstein.bernie.modules.task;

import de.christianbernstein.bernie.sdk.event.EventAPI;
import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * todo add uuid
 * @author Christian Bernstein
 */
@Data
public class TaskRunner implements EventAPI.IWithEventController<TaskRunner> {

    private final TaskStruct taskStruct;

    private final EventAPI.IEventController<TaskRunner> eventController = new EventAPI.DefaultEventController<>();

    private int pieceIndex = 0;

    @Override
    public EventAPI.@NonNull IEventController<TaskRunner> getEventController() {
        return this.eventController;
    }

    @Nullable
    public PieceStruct getPiece() {
        if (this.taskStruct.getPieces().size() <= this.pieceIndex) {
            return null;
        } else {
            final PieceStruct piece = this.taskStruct.getPieces().get(this.pieceIndex);
            if (piece != null) {
                return piece;
            } else {
                throw new NullPointerException(String.format("Pieces cannot be null. [i=%s]", this.pieceIndex));
            }
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public TaskRunner completePiece() {
        // Check if there is a piece left, or if the completePiece method is called on an already finished taskStruct
        if (this.getPiece() != null) {
            final PieceStruct completedPiece = this.getPiece();
            // Trigger piece finished event
            this.getEventController().fire(new PieceFinishedEvent(this, completedPiece));

            this.pieceIndex++;
            // Check if completed
            final PieceStruct newPiece = this.getPiece();
            if (this.getPiece() != null) {
                // There is a next piece in the list of pieces
                this.getEventController().fire(new PieceEnterContextEvent(this, newPiece));
            } else {
                // The last piece was completed
                this.getEventController().fire(new TaskCompletedEvent(this));
            }
        }
        return this;
    }
}
