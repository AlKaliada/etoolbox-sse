(function (Granite, $) {
    'use strict';

    $(document).ready(function () {
        const eventSource = new EventSource("/services/sse");
        const container = document.getElementsByClassName('foundation-layout-panel-content')[0];
        eventSource.onmessage = (message) => {
            message.data.split('!!!').forEach((line) => {
                container.append(line);
                container.append(document.createElement('br'));
            });
        }
    });

    $(document).on('click', '#editDialogButton', function () {
        console.log(`Edit dialog button clicked`);
        const $dialogTemplate = $($('#editDialog').prop('content'));
        let dialog = getConfigurationDialog($dialogTemplate);
        dialog.show();
    });

    function getConfigurationDialog(content) {
        let dialog = $('#logs-configuration');
        if (dialog.length) {
            return dialog[0];
        }
        dialog = new Coral.Dialog().set({
            id: 'logs-configuration',
            header: {
                innerHTML: 'Logs Configuration'
            },
            content: {
                innerHTML: $('<div>').append(content).html()
            }
        });
        document.body.appendChild(dialog);
        return dialog;
    }

})(Granite, Granite.$);
