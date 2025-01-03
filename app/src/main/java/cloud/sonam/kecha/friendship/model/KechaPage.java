package cloud.sonam.kecha.friendship.model;

import java.util.List;

/**
 * @author sonamwangyalsamdupkhangsar
 *
 */
public class KechaPage<T> {

    private int totalPages;
    private int page;
    private long totalElements;
    private List<T> content;

    /**
     * @param totalPages
     * @param page
     * @param totalElements
     * @param content
     */
    public KechaPage(int totalPages, int page, long totalElements,
                     List<T> content) {
        super();
        this.totalPages = totalPages;
        this.page = page;
        this.totalElements = totalElements;
        this.content = content;
    }

    public KechaPage() {

    }

    public int getTotalPages() {
        return totalPages;
    }
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }
    public long getTotalElements() {
        return totalElements;
    }
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    public List<T> getContent() {
        return content;
    }
    public void setContent(List<T> content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "KechaPage{" +
                "totalPages=" + totalPages +
                ", page=" + page +
                ", totalElements=" + totalElements +
                ", content=" + content +
                '}';
    }
}