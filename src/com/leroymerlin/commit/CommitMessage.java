package com.leroymerlin.commit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author Damien Arrachequesne <damien.arrachequesne@gmail.com>
 */
class CommitMessage {
    private static final int MAX_LINE_LENGTH = 72; // https://stackoverflow.com/a/2120040/5138796

    public static final Pattern COMMIT_FIRST_LINE_FORMAT = Pattern.compile("^(.*)(M.*)  (\\[.*\\])(.*)");
    public static final Pattern COMMIT_CLOSES_FORMAT = Pattern.compile("Closes (.+)");

    private ChangeType changeType;
    private String changeScope, shortDescription, longDescription, breakingChanges, closedIssues, zhaiyao;
    private boolean wrapText = true;
    private boolean skipCI = false;

    private CommitMessage() {
        this.longDescription = "";
        this.breakingChanges = "";
        this.closedIssues = "";
    }

    public CommitMessage(ChangeType changeType, String shortDescription, String longDescription, String zhaiyao) {
        this.changeType = changeType;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.zhaiyao = zhaiyao;
    }

    @Override
    public String toString() {
        if (isNotBlank(shortDescription)) {
            shortDescription = shortDescription.replaceAll("，", ",").replaceAll(" ", "");
        }
        StringBuilder builder = new StringBuilder();
        if (isNotBlank(shortDescription)) {
            builder
                    .append("修改单编号：")
                    .append(shortDescription.trim().toUpperCase())
                    .append("  ");
        }
        builder
                .append("[")
                .append(changeType.label())
                .append("]")
                .append(zhaiyao);

        if (isNotBlank(longDescription)) {
            builder
                    .append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(longDescription);
        }
        return builder.toString();
    }


    public static CommitMessage parse(String message) {
        CommitMessage commitMessage = new CommitMessage();

        try {
            Matcher matcher = COMMIT_FIRST_LINE_FORMAT.matcher(message);
            if (!matcher.find()) {
                return commitMessage;
            }
            commitMessage.changeType = ChangeType.valueOf(matcher.group(1).toUpperCase());
            commitMessage.changeScope = matcher.group(3);
            commitMessage.shortDescription = matcher.group(4);

            String[] strings = message.split("\n");
            if (strings.length < 2) return commitMessage;

            int pos = 1;
            StringBuilder stringBuilder;

            stringBuilder = new StringBuilder();
            for (; pos < strings.length; pos++) {
                String lineString = strings[pos];
                if (lineString.startsWith("BREAKING") || lineString.startsWith("Closes") || lineString.equalsIgnoreCase("[skip ci]"))
                    break;
                stringBuilder.append(lineString).append('\n');
            }
            commitMessage.longDescription = stringBuilder.toString().trim();

            stringBuilder = new StringBuilder();
            for (; pos < strings.length; pos++) {
                String lineString = strings[pos];
                if (lineString.startsWith("Closes") || lineString.equalsIgnoreCase("[skip ci]")) break;
                stringBuilder.append(lineString).append('\n');
            }
            commitMessage.breakingChanges = stringBuilder.toString().trim().replace("BREAKING CHANGE: ", "");

            matcher = COMMIT_CLOSES_FORMAT.matcher(message);
            stringBuilder = new StringBuilder();
            while (matcher.find()) {
                stringBuilder.append(matcher.group(1)).append(',');
            }
            if (stringBuilder.length() > 0) stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            commitMessage.closedIssues = stringBuilder.toString();

            commitMessage.skipCI = message.contains("[skip ci]");
        } catch (RuntimeException e) {
        }

        return commitMessage;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public String getChangeScope() {
        return changeScope;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public String getBreakingChanges() {
        return breakingChanges;
    }

    public String getClosedIssues() {
        return closedIssues;
    }

    public boolean isSkipCI() {
        return skipCI;
    }

    public String getZhaiyao() {
        return zhaiyao;
    }

    public void setZhaiyao(String zhaiyao) {
        this.zhaiyao = zhaiyao;
    }
}