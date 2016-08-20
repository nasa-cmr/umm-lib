(ns cmr.umm.granule.temporal
  "Provides functions for granule temporal coverage construction"
  (:require [cmr.common.services.errors :as errors]
            [cmr.umm.umm-granule :as g]))

;; default temporal date times map
(def temporal-date-times {:range-date-time nil
                          :single-date-time nil})

(defn validate-temporal-map
  "Validates the given temporal map, throws Exception if more than one of the temporal datetimes is provided"
  [temporal-map]
  (let [provided-temporal (select-keys temporal-map (keys temporal-date-times))
        non-empty-values (remove nil? (vals provided-temporal))]
    (when (> (count non-empty-values) 1)
      (errors/internal-error! "Only one of range-date-times, single-date-times and periodic-date-times can be provided."))))

(defn temporal
  "Constructs a temporal coverage record with the given map, verifies only one of the temporal datetimes can exist."
  [temporal-map]
  (validate-temporal-map temporal-map)
  (g/map->GranuleTemporal (merge temporal-date-times temporal-map)))
