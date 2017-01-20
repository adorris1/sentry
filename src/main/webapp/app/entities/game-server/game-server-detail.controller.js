(function() {
    'use strict';

    angular
        .module('sentryApp')
        .controller('GameServerDetailController', GameServerDetailController);

    GameServerDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'GameServer'];

    function GameServerDetailController($scope, $rootScope, $stateParams, previousState, entity, GameServer) {
        var vm = this;

        vm.gameServer = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('sentryApp:gameServerUpdate', function(event, result) {
            vm.gameServer = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
