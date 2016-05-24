(ns t5pmobile.leave
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [t5pmobile.core :as t5pcore]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [ajax.core :refer [GET POST]]
            [clojure.string :as str]
            [om-bootstrap.button :as b]
            [om-bootstrap.panel :as p]
            [cljs.core.async :refer [put! dropping-buffer chan take! <!]]
            [om-bootstrap.input :as i]
            
  )
  (:import goog.History)
)


(def ch (chan (dropping-buffer 2)))
(enable-console-print!)
(def jquery (js* "$"))
(defonce app-state (atom  {:leavetypes [] :leavecode "请选择"} ))



;(swap! app-state assoc-in [:leavetypes] {})
;(swap! app-state assoc-in [:leavecodes] ())
(defonce fieldnum (atom 0))

(defn fields-to-map [fielddef]
  (let [
      newdata {(keyword (get fielddef "fieldcode"))  {:name (get fielddef "name") :fieldtype (get fielddef "fieldtype") :required (get fielddef "required") :num @fieldnum  } }
    ]
    (swap! fieldnum inc)
    newdata
  )
)

(defn leaves-to-map [leave]
  (let [     
      newdata {
       (keyword (get leave "leavecode")) {
         :fields (into {} (doall  (map fields-to-map (get leave "fields"))  ) )  
         :name (get leave "name")} 
       }
    ]
    (reset! fieldnum 0)
    newdata
  )
)

(defn leaves-to-leavecodes [leave]
  (let [     
      newdata {:leavecode (get leave "leavecode") :name (get leave "name") }
    ]
    newdata
  )
  
)

(defn OnGetLeaveTypes [response]
  ( let [ 
    newdata (map leaves-to-map response)
    leavecodes (map leaves-to-leavecodes response)
  ]
     
     
     (swap! app-state assoc :leavetypes  (into {} newdata) )
     (swap! app-state assoc :leavecodes leavecodes) 
  )
   

   ;(.log js/console (:leavetypes @app-state)) 

)



(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)







(defn getLeaveTypes [data]
 (.log js/console (str "token: " " " (:token  (first (:token @t5pcore/app-state)))       ))

 
  (GET "http://localhost/T5PWebAPI/api/leavetype/leavetype2?type=0" {:handler OnGetLeaveTypes
                                            :error-handler error-handler
                                            :headers {:content-type "application/json" :Authorization (str "Bearer "  (:token  (first (:token @t5pcore/app-state)))) }
                                            })
)



(defcomponent empty-view [_ _]
  (render
    [_]
    (dom/div)
  )
)




(defn onMount [data]
  (getLeaveTypes data)
  (jquery
   (fn []
     (-> (jquery "#datepicker")
       (.datepicker {})
     )
   )
  )
)

(defn setdatepicker [field]
  (if (= (:fieldtype (nth field 1) ) 1 ) 
    (jquery
     (fn []
       (-> (jquery (str "#" (name (nth field 0) )) )
         (.datepicker {})
       )      
     )
    )  
  )
  ;(.log js/console (get field  "fieldcode"    )   )  
  
)

(defn setdatepickers []
  (let [fields  (:fields ((keyword (:leavecode @app-state)) (:leavetypes @app-state) ) ) ]
    ;(.log js/console "Inside SetDate Pickers" )
    ;(.log js/console (get (nth fields 2 ) "fieldcode"    )   )
    (dorun (map setdatepicker fields   ))
  )
)



(defn setdatepicker2 [field]
  (.log js/console (:name (nth field 1)  ) )
)


(defn setdatepickers2 []
  (let [fields (:fields ((keyword (:leavecode @app-state)) (:leavetypes @app-state) ) )  ]
    ;(.log js/console "Inside SetDate Pickers" )
    ;(.log js/console (get (nth fields 2 ) "fieldcode"    )   )
    (dorun (map setdatepicker2 fields   ))
  )
)

(defn alertselected [event]
  ;(js/alert (str event  "ClojureScript says 'Boo!'" ))
  (swap! app-state assoc :leavecode  event)

  (jquery
   (fn []
     (-> (jquery "#leavebtngroup")
       (.trigger  "click")
     )
   )
  )
  
   ;(.log js/console (str "#" "leavefromdate" )) 
  ;(setdatepickers2)

)

(defn initqueue []
  (doseq [n (range 1000)]
    (go ;(while true)
      (take! ch (fn [v] (
                         setdatepickers
                         ;.log js/console "Core.ASYNVC working!!!" 
                         )       )  )

    )
  )
)

(initqueue)
;(initqueue)
;(initqueue)

(defcomponent leave-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (did-update [this prev-props prev-state]
    ;(.log js/console "did updated!!!!!!!!!!!!!" )  
    (put! ch 42)
  )
  (render [_]
    (p/panel (merge {:header (dom/h3 "休假申请" )} {:bs-style "primary"}
      
      )

      (dom/div {:className "panel-body"}
        (dom/form {:className "form-horizontal"}
          (dom/div {:className "form-group"}
            (dom/label {:className "col-sm-2 control-label"} "类型"
              (dom/span {:style {:color "Red"}} "*")
            )
            (dom/div {:className "col-sm-10"}
              (b/button-group
                {:id "leavebtngroup" }
                (b/dropdown {:title (:leavecode @app-state) }
                  (map (fn [item]
                    (b/menu-item {:key (:leavecode item)  :on-select (fn [e](alertselected e))   } (:name item))
                    )(:leavecodes data)
                  )                  
                )
              )
            )
          )

          (map (fn [text]
            (dom/div {:className "form-group"}
              (dom/label {:className "col-sm-2 control-label"} 
                (dom/span {} (:name  (nth text 1)))
                (if ( = (:required (nth text 1)  ) true ) 
                  (dom/span {:style {:color "Red"}} "*")
                )
              )
              (dom/div {:className "col-sm-10"}
                (cond 
                  (= (:fieldtype (nth text 1)  )  0)
                    (dom/input {:type "text" :id (name (first text) ) })
                  (= (:fieldtype (nth text 1)  )  1)
                    (dom/input {:type "text" :id (name (first text) ) })
                  (= (:fieldtype (nth text 1)  )  2)
                    (dom/input {:type "text" :id (name (first text) ) })

                  (= (:fieldtype (nth text 1)  )  3)            
                    (dom/input {:type "checkbox" :label (:name  (nth text 1)) :checked true}) 

                )
                
                
              )
            )            
            )  (sort #(compare ( :num ( nth %1 1)) ( :num( nth %2 1))) (into[] (:fields ((keyword (:leavecode data)) (:leavetypes @app-state)))))   





          )



        )
      )
    )
  )
)




(sec/defroute leave-page "/leave" []
  (om/root leave-page-view
           app-state
           {:target (. js/document (getElementById "app"))}))


