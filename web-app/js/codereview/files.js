var previousExpandedForFilesChangesetId; //FIXME remove - this does not belong here, it's here for popovers setup...

function showFile(changesetId, fileId, fileChangeType, fileName) {
    appendDiff(changesetId, fileId);

    var fileContentUrl = uri.projectFile.getFileWithContent;
    fileContentUrl += fileId;
    var fileContent;
    if (fileChangeType != 'DELETE') {
        $.getJSON(fileContentUrl, function (file) {
            if (file.isText) {
                fillFileTitleTemplate(divideNameWithSlashesInTwo(file.name), changesetId, fileId);
                renderContentFileWithSyntaxHighlighter(changesetId, file, fileId);
                showFilesContent(changesetId);
            }
            else {
                showMessageAboutNonTextFile(changesetId);
            }
        });
    }
    else {
        cleanPreviousFilesContent(changesetId);
        fillFileTitleTemplate(fileName, changesetId, fileId);
        showMessageAboutRemovedFile(changesetId);
        showFilesContent(changesetId);
    }

    if (previousExpandedForFilesChangesetId != null) {
        hidePopovers(previousExpandedForFilesChangesetId);
    }
    previousExpandedForFilesChangesetId = changesetId;
}

function hideFile(changesetId, fileId) {
    $("#content-files-" + changesetId).hide();
    $('#content-files-span-' + changesetId).hide();
    $('#content-files-' + changesetId + ' .linenums li').popover("hide");
    hidePopovers(changesetId);
    $("#content-files-title-" + changesetId).hide();
}

function appendDiff(changesetId, fileId) {

    var diffUrl = "projectFile/getDiff/" + fileId;

    $.getJSON(diffUrl, function (projectDiff) {
        var diff = $("#diffTemplate").render({changesetId:changesetId});
        $("#diff-" + changesetId).html(diff);

        $.SyntaxHighlighter.init({lineNumbers:false});

        $("#diff-box-" + changesetId).html("<pre class='codeViewer'/>");
        $("#diff-box-" + changesetId + " .codeViewer")
            .html(colorizeDiff(projectDiff.rawDiff))
            .addClass("language-" + projectDiff.fileType)
            .syntaxHighlight();
    });
}

function colorizeDiff(text) {
    var lines = escapeHTML(text).split("\n");
    for (i = 0; i < lines.length; i++) {
        if (lines[i][0] == '+') {
            lines[i] = '<span style="background-color:rgba(73,203,30,0.69)">' + lines[i] + "</span>";
        }
        else if (lines[i][0] == '-') {
            lines[i] = '<span style="background-color:rgba(217,52,51,0.82)">' + lines[i] + "</span>";
        }
        else
            lines[i] = '<span>' + lines[i] + '</span>'
    }
    return lines.join("\n");
}

function escapeHTML(text) {
    return $('<div/>').text(text).html();
}

function showDiff(changesetId) {
    $("#diff-box-" + changesetId).show(100);
    $("#button-hiding-diff-" + changesetId).show(100);
    $("#button-showing-diff-" + changesetId).hide();
}

function hideDiff(changesetId) {
    $("#diff-box-" + changesetId).hide(100);
    $("#button-showing-diff-" + changesetId).show(100);
    $("#button-hiding-diff-" + changesetId).hide();
}

function showFilesContent(changesetId) {
    $('#content-files-span-' + changesetId).show();
}

function cleanPreviousFilesContent(changesetId) {
    $("#content-files-" + changesetId).html("");
}

function setContentFilesTitle(changesetId, title) {
    $("#content-files-title-" + changesetId).html(title);
}

function showMessageAboutNonTextFile(changesetId) {
    $("#content-files-" + changesetId).html("<pre class='codeViewer'/>");
    $("#content-files-" + changesetId + " .codeViewer")
        .html("<h3>This file isn't text file.</h3>")
}

function fillFileTitleTemplate(fileName, changesetId, fileId) {
    var title = $("#fileTitleTemplate").render({
        fileName:fileName,
        changesetId:changesetId,
        fileId:fileId
    });
    setContentFilesTitle(changesetId, title);
}

function showMessageAboutRemovedFile(changesetId) {
    $("#content-files-" + changesetId).html("<pre class='codeViewer'/>");
    $("#content-files-" + changesetId + " .codeViewer")
        .html("<h3>This file was removed.</h3>")
}

function attachLineCommentPopover(changesetId, fileId) {
    $('#content-files-' + changesetId + ' .linenums li').each(function (i, element, ignored) {
        $(element).click(function () {
            $('#content-files-' + changesetId + ' .linenums li').popover("hide");
            $(element).popover("show");
        });
        //TODO check if creating the content of the popover (i.e. commentForm) can be deferred to popover activation
        var commentForm = $("#addLineCommentFormTemplate").render({fileId:fileId, changesetId:changesetId, lineNumber:i + 1 });
        $(element).popover({content:commentForm, placement:"left", trigger:"manual" });
    });
}

function renderContentFileWithSyntaxHighlighter(changesetId, file, fileId) {
    $.SyntaxHighlighter.init({lineNumbers:true});
    $("#content-files-" + changesetId).html("<pre class='codeViewer'/>");
    $("#content-files-" + changesetId + " .codeViewer")
        .text(file.content)
        .addClass("language-" + file.filetype)
        .syntaxHighlight();
    attachLineCommentPopover(changesetId, fileId);
}
