(ns cmr.umm.dif.collection.temporal
  "Contains functions for parsing and generating the Temporal_Coverage element of DIF dialect."
  (:require [clojure.data.xml :as x]
            [cmr.common.xml :as cx]
            [cmr.common.date-time-parser :as parser]
            [cmr.umm.collection :as c]
            [cmr.umm.generator-util :as gu]))

(defn string->datetime
  "convert the string to joda datetime if it is in either DateTime or Date format."
  [datetime-string]
  (when datetime-string
    (parser/parse-datetime datetime-string)))

(defn xml-elem->Temporal
  "Returns a list of UMM RangeDateTimes from a parsed DIF XML structure"
  [collection-element]
  (let [elements (cx/elements-at-path collection-element [:Temporal_Coverage])]
    (when-not (empty? elements)
      (let [range-date-times (map #(c/map->RangeDateTime
                                     {:beginning-date-time (string->datetime (cx/string-at-path % [:Start_Date]))
                                      :ending-date-time (string->datetime (cx/string-at-path % [:Stop_Date]))})
                                  elements)]
        (c/map->Temporal {:range-date-times range-date-times
                          :single-date-times []
                          :periodic-date-times []})))))

(defn generate-temporal
  "Generates the Temporal_Coverage element of DIF XML from a UMM Collection temporal record."
  [temporal]
  (when temporal
    (for [range-date-time (:range-date-times temporal)]
      (let [{:keys [beginning-date-time ending-date-time]} range-date-time]
        (x/element :Temporal_Coverage {}
                   (when beginning-date-time
                     (x/element :Start_Date {} (str beginning-date-time)))
                   (when ending-date-time
                     (x/element :Stop_Date {} (str ending-date-time))))))))
