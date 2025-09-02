package de.lacertis.loreutils.lectern;

import de.lacertis.loreutils.config.ModConfig;

import java.util.Objects;

public final class LecternCopyOptions {
    private final CopyAction action;
    private final OutputMode mode;
    private final int pageIndex;
    private final boolean includeTitleAuthor;
    private final boolean numberPages;
    private final String pageSeparator;
    private final boolean formattedKeepPageBreaks;
    private final boolean exportToFile;
    private final String filePattern;

    private LecternCopyOptions(
            CopyAction action,
            OutputMode mode,
            int pageIndex,
            boolean includeTitleAuthor,
            boolean numberPages,
            String pageSeparator,
            boolean formattedKeepPageBreaks,
            boolean exportToFile,
            String filePattern
    ) {
        this.action = Objects.requireNonNull(action, "action");
        this.mode = Objects.requireNonNull(mode, "mode");
        this.pageIndex = Math.max(0, pageIndex);
        this.includeTitleAuthor = includeTitleAuthor;
        this.numberPages = numberPages;
        this.pageSeparator = Objects.requireNonNull(pageSeparator, "pageSeparator");
        this.formattedKeepPageBreaks = formattedKeepPageBreaks;
        this.exportToFile = exportToFile;
        this.filePattern = Objects.requireNonNull(filePattern, "filePattern");
    }

    public static LecternCopyOptions fromConfigAndContext(ModConfig cfg, int currentPage, CopyAction overrideAction, OutputMode overrideMode) {
        Objects.requireNonNull(cfg, "cfg");
        final CopyAction action = overrideAction != null ? overrideAction : cfg.defaultLecternAction;
        final OutputMode mode = overrideMode != null ? overrideMode : cfg.defaultLecternMode;
        final int pageIndex = Math.max(0, currentPage);
        final String sep = cfg.pageSeparator != null ? cfg.pageSeparator : "\\n\\n---\\n\\n";
        final boolean keepPageBreaks = cfg.formattedKeepPageBreaks;
        return new LecternCopyOptions(
                action,
                mode,
                pageIndex,
                cfg.includeTitleAuthor,
                cfg.numberPages,
                sep,
                keepPageBreaks,
                cfg.exportToFile,
                cfg.filePattern != null ? cfg.filePattern : "{title} - {author} - {sha1}.{ext}"
        );
    }

    public CopyAction getAction() { return action; }

    public OutputMode getMode() { return mode; }

    public int getPageIndex() { return pageIndex; }

    public boolean isIncludeTitleAuthor() { return includeTitleAuthor; }

    public boolean isNumberPages() { return numberPages; }

    public String getPageSeparator() { return pageSeparator; }

    public boolean isFormattedKeepPageBreaks() { return formattedKeepPageBreaks; }

    public boolean isExportToFile() { return exportToFile; }

    public String getFilePattern() { return filePattern; }

    public String fileExtension() { return "txt"; }

    public String normalizedSeparator() {
        return pageSeparator
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\r", "\r");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LecternCopyOptions that)) return false;
        return pageIndex == that.pageIndex &&
                includeTitleAuthor == that.includeTitleAuthor &&
                numberPages == that.numberPages &&
                formattedKeepPageBreaks == that.formattedKeepPageBreaks &&
                exportToFile == that.exportToFile &&
                action == that.action &&
                mode == that.mode &&
                Objects.equals(pageSeparator, that.pageSeparator) &&
                Objects.equals(filePattern, that.filePattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, mode, pageIndex, includeTitleAuthor, numberPages, pageSeparator, formattedKeepPageBreaks, exportToFile, filePattern);
    }

    @Override
    public String toString() {
        return "LecternCopyOptions{" +
                "action=" + action +
                ", mode=" + mode +
                ", pageIndex=" + pageIndex +
                ", includeTitleAuthor=" + includeTitleAuthor +
                ", numberPages=" + numberPages +
                ", pageSeparator='" + pageSeparator + '\'' +
                ", formattedKeepPageBreaks=" + formattedKeepPageBreaks +
                ", exportToFile=" + exportToFile +
                ", filePattern='" + filePattern + '\'' +
                '}';
    }
}
