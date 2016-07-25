(ns t5pmobile.home
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [t5pmobile.core :as t5pcore]
            [ajax.core :refer [GET POST]]
            
  )
  (:import goog.History)
)

(enable-console-print!)


(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))




(defcomponent home-page-view [data owner]
  (render
    [_]
    (dom/div
      (om/build t5pcore/website-view data {})
      (dom/div {:className "panel panel-primary"}
        (dom/div {:className "panel-heading"}
                   "基本信息" 
        )
        (dom/table {:className "table table-bordered"}
          (dom/tbody
            (dom/tr
              (dom/td #js {:rowSpan "3" :className "portrait"}
                (dom/img {:src "http://localhost/T5PWebAPI/Content/Portrait/charles.jpg" :className "img-rounded portrait"})
              )
              (dom/td {:className "tdtable"} (:EmpName (:Employee data) ) 
              )
            )
            (dom/tr {:className "table_tr_background"}
              (dom/td {:className "tdtable"} "总经理" )
            )
          )
        )
      )


      (dom/div {:className "panel panel-primary"}
        (dom/div {:className "panel-heading"}
                   "年假" 
        )
        (dom/table {:className "table table-bordered"}
          (dom/tbody
            (dom/tr
              (dom/td 
                (dom/div {:style {:float "left" }} "全年共享受年假") 
                (dom/div {:style {:float "right"}} "0 天")
              )
            )
            (dom/tr {:className "table_tr_background"}
              (dom/td 
                (dom/div {:style {:float "left"}} "全年已用" )
                (dom/div {:style {:float "right"}} "0 天" )
              )
            )
            (dom/tr
              (dom/td 
                (dom/div {:style {:float "left"}} "余额" )
                (dom/div {:style {:float "right"}} "0 天" )
              )
            )
          )
        )
      )
      (dom/div {:className "panel panel-primary"}
          (dom/div {:className "panel-heading"}
                     "排班" 
          )
          (dom/ul {:className "list-group"}
            (map (fn [text]
              (dom/div
                (dom/li {:className "list-group-item"}
                  (dom/span {:className "glyphicon glyphicon-time grey"})
                  (get text "workdate")
                )
                (dom/li {:className "list-group-item paddingleft3 table_tr_background gray"} "没有排班")
              )
              ) (:Roster (:Employee data))
            )
          )
      )      
    )
  )
)


(sec/defroute home-page "/home" []
  (om/root home-page-view
           t5pcore/app-state
           {:target (. js/document (getElementById "app"))}))






