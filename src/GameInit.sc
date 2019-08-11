;;; Sierra Script 1.0 - (do not remove this comment)
;
;	GAMEINIT.SC
;
;	Add things to initialize at game start here.
;	Make sure they don't require any objects or methods in MAIN.SC.
;
;
;	Initializing things here means that the code can be disposed at completion,
;	as it will not be used again. This was done in Quest for Glory I.
;

(script# GAME_INIT)
(include game.sh)
(use Main)
(use Save)
(use User)
(use System)

(public
	gameInitCode 0
)

(instance gameInitCode of Code
	(method (init)
		(= debugging TRUE) ;Set to TRUE if you want to enable the debug features.
		(SysWindow
			;These colors can be changed to suit your preferences.
			color: (= curTextColor vBLACK)
			back: (= curBackColor vWHITE)
		)
		(= colorCount (Graph GDetect))
		(= systemWindow SysWindow)
		(= musicChannels (DoSound NumVoices))
		(= useSortedFeatures TRUE)
		(= possibleScore 0)	;Set the maximum score here
		(= showStyle HSHUTTER)
		(DisposeScript GAME_INIT)	;and finally, trash this script from memory
	)
)