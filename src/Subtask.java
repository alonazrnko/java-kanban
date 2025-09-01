public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (epicId == this.id) {
            return;
        }
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return String.format("Subtask{id=%d, title='%s', epicId=%d}", id, title, epicId);
    }
}
