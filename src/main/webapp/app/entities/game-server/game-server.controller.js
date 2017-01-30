(function() {
    'use strict';

    angular
        .module('sentryApp')
        .controller('GameServerController', GameServerController);

    GameServerController.$inject = ['$scope', '$state', '$interval', 'GameServer', 'ParseLinks', 'AlertService', 'paginationConstants'];

    function GameServerController ($scope, $state, $interval, GameServer, ParseLinks, AlertService, paginationConstants) {
        var vm = this;

        vm.gameServers = [];
        vm.loadPage = loadPage;
        vm.itemsPerPage = 60;
        vm.page = 0;
        vm.links = {
            last: 0
        };
        vm.predicate = 'name';
        vm.reset = reset;
        vm.reverse = true;

        vm.shortName = shortName;
        vm.pingToText = pingToText;
        vm.grayIfZero = grayIfZero;
        vm.formatMap = formatMap;
        vm.pingToClass = pingToClass;
        vm.grayIfFalse = grayIfFalse;
        vm.prettyBoolText = prettyBoolText;
        vm.prettyBoolClass = prettyBoolClass;

        vm.expireWarning = expireWarning;
        vm.pingWarning = pingWarning;
        vm.rconWarning = rconWarning;
        vm.rconWarningText = rconWarningText;

        vm.humanize = humanize;

        loadAll();

        vm.refresher = $interval(loadAll, 60000);
        vm.nextRefresh = 60;
        vm.clock = $interval(updateTime, 1000);

        $scope.$on('$destroy', function() {
            $interval.cancel(vm.refresher);
            $interval.cancel(vm.clock);
        });

        function updateTime() {
            if (vm.nextRefresh === 0) {
                vm.nextRefresh = 60;
            }
            vm.nextRefresh--;
        }

        function loadAll () {
            GameServer.query({
                page: vm.page,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'name') {
                    result.push('name');
                }
                return result;
            }

            function onSuccess(data, headers) {
                vm.gameServers = [];
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                for (var i = 0; i < data.length; i++) {
                    vm.gameServers.push(data[i]);
                }
            }

            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function reset () {
            vm.page = 0;
            vm.gameServers = [];
            loadAll();
        }

        function loadPage(page) {
            vm.page = page;
            loadAll();
        }

        function shortName(value) {
            return value.replace(/(^[A-Za-z]{3})[^0-9]*([0-9]+).*/, "$1$2").toUpperCase();
        }

        function pingToText(ping) {
            if (ping > 0 && ping < 1000) {
                return 'UP';
            } else {
                return 'DOWN';
            }
        }

        function grayIfZero(number) {
            if (number > 0) {
                return '';
            } else {
                return 'gray';
            }
        }

        function formatMap(name) {
            if (name != null && name != '') {
                return ' @ ' + name;
            } else {
                return '';
            }
        }

        function pingToClass(ping) {
            if (ping > 0 && ping < 1000) {
                return 'label-success';
            } else if (ping < 2000) {
                return 'label-warning';
            } else {
                return 'label-danger';
            }
        }

        function grayIfFalse(flag) {
            if (flag === true) {
                return '';
            } else {
                return 'gray';
            }
        }

        function prettyBoolText(value) {
            if (value) {
                return 'YES';
            } else {
                return 'NO';
            }
        }

        function prettyBoolClass(value) {
            if (value) {
                return 'label-warning';
            } else {
                return 'label-info';
            }
        }

        function expireWarning(endTime) {
            var now = moment();
            var end = moment(endTime);
            var duration = moment.duration(now.diff(end));
            var minutes = duration.asMinutes();
            if (end.isBefore(now)) {
                return 'gray';
            } else if (minutes <= 30) {
                return 'red';
            } else {
                return '';
            }
        }

        function pingWarning(end) {
            var duration = moment.duration(moment(end).diff(moment()));
            var minutes = duration.asMinutes();
            if (minutes > 2) {
                return 'red';
            } else {
                return '';
            }
        }

        function rconWarning(rcon) {
            if (!rcon) {
                return 'label label-warning';
            } else {
                return '';
            }
        }

        function rconWarningText(rcon) {
            if (!rcon) {
                return 'Missing RCON!';
            } else {
                return rcon;
            }
        }

        function humanize(date) {
            if (date == null) {
                return '';
            } else {
                return moment.duration(moment(date).diff(moment())).humanize(true);
            }
        }
    }
})();
